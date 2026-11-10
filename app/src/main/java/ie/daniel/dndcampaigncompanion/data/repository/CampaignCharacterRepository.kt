package ie.daniel.dndcampaigncompanion.data.repository

import androidx.room.withTransaction
import ie.daniel.dndcampaigncompanion.data.local.AppDatabase
import ie.daniel.dndcampaigncompanion.data.local.entity.CampaignCharacterEntity
import ie.daniel.dndcampaigncompanion.data.local.entity.CampaignRole
import ie.daniel.dndcampaigncompanion.data.local.entity.CampaignStatus
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * Handles campaign character assignments.
 *
 * This repository manages assigning characters to campaigns, retrieving
 * active assignments and viewing character participation history.
 * It also handles leaving campaigns and updating campaign-specific character information.
 */
class CampaignCharacterRepository(
    private val database: AppDatabase
) {

    // DAOs used to access campaign characters, characters, campaigns and memberships.
    private val campaignCharacterDao = database.campaignCharacterDao()
    private val characterDao = database.characterDao()
    private val campaignDao = database.campaignDao()
    private val membershipDao = database.campaignMembershipDao()

    // Retrieves the active character assignments belonging to a campaign.
    // The Flow allows the UI to receive updates when assignments change.
    fun getActiveCampaignCharacters(
        campaignId: String
    ): Flow<List<CampaignCharacterEntity>> {
        return campaignCharacterDao.getActiveCampaignCharacters(campaignId)
    }

    // Retrieves the active character assignment for a specific user in a campaign.
    // Returns null if the user does not currently have an active character.
    suspend fun getActiveCharacterForUser(
        campaignId: String,
        userId: String
    ): CampaignCharacterEntity? {
        return campaignCharacterDao.getActiveCharacterForUser(
            campaignId,
            userId
        )
    }

    // Retrieves the complete campaign participation history for a character.
    // This includes previous assignments as well as any current active assignment.
    fun getCharacterParticipationHistory(
        characterId: String
    ): Flow<List<CampaignCharacterEntity>> {
        return campaignCharacterDao.getCharacterParticipationHistory(characterId)
    }

    // Assigns a character to a user within a campaign.
    suspend fun assignCharacter(
        campaignId: String,
        characterId: String,
        userId: String
    ): CampaignCharacterEntity {

        // A transaction ensures that all database operations succeed together.
        // If an operation fails, the changes made during the transaction are rolled back.
        return database.withTransaction {

            // Checks that the campaign exists before attempting to assign a character.
            val campaign = campaignDao.getCampaignById(campaignId)
                ?: throw IllegalArgumentException("Campaign not found.")

            // Only active campaigns can receive new character assignments.
            require(campaign.status == CampaignStatus.ACTIVE) {
                "Characters cannot be assigned to an inactive campaign."
            }

            // Checks that the user has an active membership in the campaign.
            // A user must belong to the campaign before assigning a character to it.
            val membership = membershipDao.getActiveMembership(
                campaignId,
                userId
            ) ?: throw IllegalArgumentException(
                "You must be an active campaign member to assign a character."
            )

            // Only regular players can assign player characters.
            // Dungeon Masters are not allowed to assign a player character.
            require(membership.role == CampaignRole.PLAYER) {
                "The Dungeon Master cannot assign a player character."
            }

            // Checks whether the user already has an active character in this campaign.
            // This prevents a player from having multiple active characters at the same time.
            require(
                campaignCharacterDao.getActiveCharacterForUser(
                    campaignId,
                    userId
                ) == null
            ) {
                "You already have an active character in this campaign."
            }

            // Retrieves the character and checks that it belongs to the user
            // and has not been soft deleted.
            val character = characterDao.getUserCharacter(
                userId,
                characterId
            ) ?: throw IllegalArgumentException(
                "Character not found or does not belong to this user."
            )

            // Checks whether this character has previously been assigned
            // to the same campaign by the same user.
            val previousAssignment =
                campaignCharacterDao.getPreviousCharacterAssignment(
                    campaignId,
                    userId,
                    characterId
                )

            if (previousAssignment != null) {

                // Reactivates the previous assignment instead of creating a new one.
                // This preserves the original assignment record and its participation history.
                campaignCharacterDao.reactivateAssignment(
                    previousAssignment.id
                )

                // Retrieves the updated assignment so the returned object reflects
                // its current active status.
                return@withTransaction campaignCharacterDao
                    .getCampaignCharacterById(previousAssignment.id)
                    ?: throw IllegalStateException(
                        "Could not retrieve the reactivated character assignment."
                    )
            }

            // Creates a new campaign character assignment.
            // A random UUID is used to generate a unique ID for the assignment.
            // The character's ability scores are copied into the assignment so they
            // can be stored separately for this particular campaign.
            val assignment = CampaignCharacterEntity(
                id = UUID.randomUUID().toString(),
                campaignId = campaignId,
                characterId = characterId,
                userId = userId,
                strength = character.strength,
                dexterity = character.dexterity,
                constitution = character.constitution,
                intelligence = character.intelligence,
                wisdom = character.wisdom,
                charisma = character.charisma,
                joinedAt = System.currentTimeMillis()
            )

            // Saves the new assignment in the database.
            campaignCharacterDao.insertCampaignCharacter(assignment)

            // Returns the newly created assignment.
            assignment
        }
    }

    // Marks a user's active character assignment as inactive when they leave a campaign.
    suspend fun leaveCampaign(
        campaignId: String,
        userId: String
    ) {

        // Uses a transaction to keep the lookup and update together.
        database.withTransaction {

            // Finds the user's active character assignment.
            // Throws an exception if the user does not currently have one.
            val assignment = campaignCharacterDao.getActiveCharacterForUser(
                campaignId,
                userId
            ) ?: throw IllegalArgumentException(
                "You do not have an active character in this campaign."
            )

            // Marks the assignment as inactive and records when the user left.
            // The assignment remains in the database so its history is preserved.
            campaignCharacterDao.leaveCampaign(
                assignmentId = assignment.id,
                leftAt = System.currentTimeMillis()
            )
        }
    }

    // Updates the campaign-specific information stored for a character assignment.
    suspend fun updateCampaignCharacter(
        assignment: CampaignCharacterEntity
    ) {

        // Retrieves the existing assignment so its current state and identity
        // can be checked before allowing any changes.
        val existingAssignment =
            campaignCharacterDao.getCampaignCharacterById(assignment.id)
                ?: throw IllegalArgumentException(
                    "Character assignment not found."
                )

        // Prevents changes to assignments that are no longer active.
        require(existingAssignment.isActive) {
            "Inactive character assignments cannot be edited."
        }

        // Ensures that the assignment's identity and participation history
        // cannot be changed during an update.
        // Only campaign-specific character information can be modified.
        require(
            existingAssignment.campaignId == assignment.campaignId &&
                    existingAssignment.characterId == assignment.characterId &&
                    existingAssignment.userId == assignment.userId &&
                    existingAssignment.joinedAt == assignment.joinedAt
        ) {
            "The identity and participation history of an assignment cannot be changed."
        }

        // Saves the updated campaign character information.
        campaignCharacterDao.updateCampaignCharacter(assignment)
    }
}
