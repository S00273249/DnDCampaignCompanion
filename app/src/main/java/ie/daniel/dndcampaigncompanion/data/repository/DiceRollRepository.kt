package ie.daniel.dndcampaigncompanion.data.repository

import androidx.room.withTransaction
import ie.daniel.dndcampaigncompanion.data.local.AppDatabase
import ie.daniel.dndcampaigncompanion.data.local.entity.DiceRollEntity
import ie.daniel.dndcampaigncompanion.data.local.entity.CampaignStatus
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * Handles dice roll history for campaigns and sessions.
 *
 * This repository retrieves previous dice rolls and saves new results.
 * It also checks campaign status and user membership before allowing
 * a die roll to be recorded.
 */
class DiceRollRepository(
    private val database: AppDatabase
) {

    // DAOs used to access dice rolls, campaigns, memberships and sessions.
    private val diceRollDao = database.diceRollDao()
    private val campaignDao = database.campaignDao()
    private val membershipDao = database.campaignMembershipDao()
    private val sessionDao = database.sessionDao()

    // Observes all dice rolls recorded in a specific campaign.
    // The Flow allows the UI to receive updates when new rolls are saved.
    fun getCampaignDiceRolls(
        campaignId: String
    ): Flow<List<DiceRollEntity>> {
        return diceRollDao.getCampaignDiceRolls(campaignId)
    }

    // Observes dice rolls made by a specific user within a campaign.
    fun getUserDiceRolls(
        campaignId: String,
        userId: String
    ): Flow<List<DiceRollEntity>> {
        return diceRollDao.getUserDiceRolls(campaignId, userId)
    }

    // Observes dice rolls associated with a particular session.
    fun getSessionDiceRolls(
        campaignId: String,
        sessionId: String
    ): Flow<List<DiceRollEntity>> {
        return diceRollDao.getSessionDiceRolls(campaignId, sessionId)
    }

    // Retrieves the most recent dice rolls for a campaign.
    // The default limit is 20, but a different limit can be supplied.
    suspend fun getRecentDiceRolls(
        campaignId: String,
        limit: Int = 20
    ): List<DiceRollEntity> {

        // Prevents invalid limits from being passed to the DAO.
        require(limit > 0) {
            "The result limit must be greater than zero."
        }

        return diceRollDao.getRecentDiceRolls(campaignId, limit)
    }

    // Saves the result of a die roll to the database.
    suspend fun saveDiceRoll(
        campaignId: String,
        userId: String,
        diceSelectionsJson: String,
        individualResultsJson: String,
        modifier: Int,
        subtotal: Int,
        total: Int,
        description: String? = null
    ): DiceRollEntity {

        // Makes sure the selected dice information has been provided.
        // The selections are stored as a JSON string so multiple dice selections
        // can be represented in a single database field.
        require(diceSelectionsJson.isNotBlank()) {
            "Dice selections cannot be empty."
        }

        // Makes sure the individual dice results have been provided.
        // These results are also stored as a JSON string.
        require(individualResultsJson.isNotBlank()) {
            "Individual dice results cannot be empty."
        }

        // A transaction ensures that the checks and insertion happen together.
        // If saving the roll fails, the transaction is rolled back.
        return database.withTransaction {

            // Checks that the campaign exists before recording the roll.
            val campaign = campaignDao.getCampaignById(campaignId)
                ?: throw IllegalArgumentException("Campaign not found.")

            // Dice rolls can only be recorded while the campaign is active.
            require(campaign.status == CampaignStatus.ACTIVE) {
                "Dice rolls cannot be recorded in an inactive campaign."
            }

            // Checks that the user currently belongs to the campaign.
            // Inactive members cannot record new dice rolls.
            require(membershipDao.isActiveMember(campaignId, userId)) {
                "You must be an active campaign member to record dice rolls."
            }

            // Retrieves the campaign's active session, if one exists.
            // If there is no active session, the dice roll is still saved
            // but its sessionId will be null.
            val activeSession = sessionDao.getActiveSession(campaignId)

            // Creates a new dice roll record with a unique ID.
            val diceRoll = DiceRollEntity(
                id = UUID.randomUUID().toString(),
                campaignId = campaignId,
                userId = userId,
                sessionId = activeSession?.id,
                diceSelectionsJson = diceSelectionsJson,
                individualResultsJson = individualResultsJson,
                modifier = modifier,
                subtotal = subtotal,
                total = total,

                // Removes unnecessary whitespace from the description.
                // If the description is null or empty after trimming, it is stored as null.
                description = description?.trim()?.takeIf { it.isNotEmpty() },

                // Records when the dice roll was made.
                rolledAt = System.currentTimeMillis()
            )

            // Saves the completed dice roll record in the database.
            diceRollDao.insertDiceRoll(diceRoll)

            // Returns the newly created dice roll.
            diceRoll
        }
    }

    // Retrieves the total number of dice rolls recorded in a campaign.
    suspend fun getCampaignDiceRollCount(campaignId: String): Int {
        return diceRollDao.getCampaignDiceRollCount(campaignId)
    }
}
