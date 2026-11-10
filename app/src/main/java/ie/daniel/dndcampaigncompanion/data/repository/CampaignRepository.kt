package ie.daniel.dndcampaigncompanion.data.repository

import androidx.room.withTransaction
import ie.daniel.dndcampaigncompanion.data.local.AppDatabase
import ie.daniel.dndcampaigncompanion.data.local.entity.CampaignEntity
import ie.daniel.dndcampaigncompanion.data.local.entity.CampaignMembershipEntity
import ie.daniel.dndcampaigncompanion.data.local.entity.CampaignStatus
import ie.daniel.dndcampaigncompanion.data.local.entity.CampaignRole
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * Handles campaign-related database operations.
 *
 * This repository manages campaign creation, retrieval, editing and status changes.
 * It also handles the initial membership created for the user who creates a campaign.
 */
class CampaignRepository(
    private val database: AppDatabase
) {

    // DAOs used to access campaign information and campaign memberships.
    private val campaignDao = database.campaignDao()
    private val membershipDao = database.campaignMembershipDao()

    // Observes campaigns created by a specific user.
    // The Flow allows the UI to receive updates when campaign records change.
    fun getCampaignsCreatedByUser(userId: String): Flow<List<CampaignEntity>> {
        return campaignDao.getCampaignsCreatedByUser(userId)
    }

    // Observes all campaigns that a user currently belongs to.
    fun getCampaignsForUser(userId: String): Flow<List<CampaignEntity>> {
        return campaignDao.getCampaignsForUser(userId)
    }

    // Observes a campaign using its ID.
    // Returns null if the campaign does not exist.
    // Changes to the campaign are automatically emitted through the Flow.
    fun observeCampaignById(campaignId: String): Flow<CampaignEntity?> {
        return campaignDao.observeCampaignById(campaignId)
    }

    // Retrieves a campaign once using its ID.
    // Unlike observeCampaignById(), this does not continuously observe changes.
    suspend fun getCampaignById(campaignId: String): CampaignEntity? {
        return campaignDao.getCampaignById(campaignId)
    }

    // Creates a new campaign and automatically assigns its creator as the Dungeon Master.
    suspend fun createCampaign(
        name: String,
        description: String? = null,
        setting: String? = null,
        location: String? = null,
        startingDate: Long,
        maxPlayers: Int,
        createdByUserId: String,
        coverImageUri: String? = null
    ): CampaignEntity {

        // Makes sure the campaign has a name.
        require(name.isNotBlank()) {
            "Campaign name cannot be empty."
        }

        // Restricts the maximum number of players to a value between 1 and 12.
        require(maxPlayers in 1..12) {
            "Maximum players must be between 1 and 12."
        }

        // Uses the same timestamp for the campaign's creation and last update times.
        val currentTime = System.currentTimeMillis()

        // Creates the campaign record with a unique ID.
        val campaign = CampaignEntity(
            id = UUID.randomUUID().toString(),
            name = name.trim(),
            description = description?.trim()?.takeIf { it.isNotEmpty() },
            coverImageUri = coverImageUri,
            setting = setting?.trim()?.takeIf { it.isNotEmpty() },
            location = location?.trim()?.takeIf { it.isNotEmpty() },
            startingDate = startingDate,
            maxPlayers = maxPlayers,
            createdByUserId = createdByUserId,
            status = CampaignStatus.ACTIVE,
            createdAt = currentTime,
            updatedAt = currentTime
        )

        // Creates a membership for the campaign creator.
        // The creator is automatically assigned the Dungeon Master role.
        val membership = CampaignMembershipEntity(
            id = UUID.randomUUID().toString(),
            campaignId = campaign.id,
            userId = createdByUserId,
            role = CampaignRole.DM,
            joinedAt = currentTime
        )

        // Saves the campaign and its creator's membership in a single transaction.
        // This prevents a campaign from being created without its Dungeon Master
        // if either database operation fails.
        database.withTransaction {
            campaignDao.insertCampaign(campaign)
            membershipDao.insertMembership(membership)
        }

        // Returns the newly created campaign.
        return campaign
    }

    // Updates the details of an existing campaign.
    // Only the current Dungeon Master can edit an active campaign.
    suspend fun updateCampaign(
        campaignId: String,
        editorUserId: String,
        name: String,
        description: String?,
        setting: String?,
        location: String?,
        startingDate: Long,
        maxPlayers: Int
    ) {

        // Validates the campaign details before accessing the database.
        require(name.isNotBlank()) {
            "Campaign name cannot be empty."
        }

        // Makes sure a valid starting date has been provided.
        require(startingDate > 0L) {
            "A valid starting date is required."
        }

        // Keeps the maximum player limit within the allowed range.
        require(maxPlayers in 1..12) {
            "Maximum players must be between 1 and 12."
        }

        // A transaction ensures that the campaign is checked and updated together.
        database.withTransaction {

            // Retrieves the campaign that is being edited.
            // Throws an exception if no matching campaign exists.
            val campaign = campaignDao.getCampaignById(campaignId)
                ?: throw IllegalArgumentException("Campaign not found.")

            // Only active campaigns can have their details edited.
            require(campaign.status == CampaignStatus.ACTIVE) {
                "Only active campaigns can be edited."
            }

            // Retrieves the editor's active membership in this campaign.
            val membership = membershipDao.getActiveMembership(
                campaignId,
                editorUserId
            )

            // Checks that the editor is the current Dungeon Master.
            // Being an ordinary campaign member is not enough to edit campaign details.
            require(membership?.role == CampaignRole.DM) {
                "Only the current Dungeon Master can edit this campaign."
            }

            // Retrieves the number of players currently in the campaign.
            val activePlayerCount =
                membershipDao.getActivePlayerCount(campaignId)

            // Prevents the maximum player limit from being reduced below
            // the number of players who are already active in the campaign.
            require(maxPlayers >= activePlayerCount) {
                "Maximum players cannot be lower than the current number of players."
            }

            // Creates an updated copy of the campaign.
            // Fields not specified here, such as the creator and creation time,
            // remain unchanged.
            campaignDao.updateCampaign(
                campaign.copy(
                    name = name.trim(),
                    description = description?.trim()
                        ?.takeIf { it.isNotEmpty() },
                    setting = setting?.trim()
                        ?.takeIf { it.isNotEmpty() },
                    location = location?.trim()
                        ?.takeIf { it.isNotEmpty() },
                    startingDate = startingDate,
                    maxPlayers = maxPlayers,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    // Changes the status of an existing campaign.
    // The current time is recorded as the campaign's last update time.
    suspend fun updateCampaignStatus(
        campaignId: String,
        status: CampaignStatus
    ) {
        campaignDao.updateCampaignStatus(
            campaignId,
            status,
            System.currentTimeMillis()
        )
    }

    // Checks whether a campaign exists in the database.
    suspend fun campaignExists(campaignId: String): Boolean {
        return campaignDao.campaignExists(campaignId)
    }
}
