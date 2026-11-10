package ie.daniel.dndcampaigncompanion.data.repository

import androidx.room.withTransaction
import ie.daniel.dndcampaigncompanion.data.local.AppDatabase
import ie.daniel.dndcampaigncompanion.data.local.entity.CampaignMembershipEntity
import ie.daniel.dndcampaigncompanion.data.local.entity.CampaignRole
import ie.daniel.dndcampaigncompanion.data.local.entity.CampaignStatus
import ie.daniel.dndcampaigncompanion.data.local.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * Handles campaign membership operations.
 *
 * This repository manages users joining and leaving campaigns, adding players
 * as a Dungeon Master and retrieving membership information.
 * It also checks campaign status, membership roles and player limits before
 * allowing membership changes.
 */
class CampaignMembershipRepository(
    private val database: AppDatabase
) {

    // DAOs used to access campaign memberships and campaign information.
    private val membershipDao = database.campaignMembershipDao()
    private val campaignDao = database.campaignDao()

    // Retrieves all active members belonging to a campaign.
    // The Flow allows the UI to receive updates when membership records change.
    fun getActiveCampaignMembers(
        campaignId: String
    ): Flow<List<CampaignMembershipEntity>> {
        return membershipDao.getActiveCampaignMembers(campaignId)
    }

    // Retrieves the complete membership history for a campaign.
    // This includes both active and inactive memberships.
    fun getCampaignMembershipHistory(
        campaignId: String
    ): Flow<List<CampaignMembershipEntity>> {
        return membershipDao.getCampaignMembershipHistory(campaignId)
    }

    // Retrieves all active campaign memberships belonging to a user.
    // This can be used to find which campaigns a user currently belongs to.
    fun getUserActiveMemberships(
        userId: String
    ): Flow<List<CampaignMembershipEntity>> {
        return membershipDao.getUserActiveMemberships(userId)
    }

    // Retrieves user profiles that are not currently active members of a campaign.
    // This can be used when selecting profiles to add to an existing campaign.
    fun getProfilesAvailableForCampaign(
        campaignId: String
    ): Flow<List<UserProfileEntity>> {
        return membershipDao.getProfilesAvailableForCampaign(campaignId)
    }

    // Retrieves the active membership for a specific user in a campaign.
    // Returns null if the user does not currently have an active membership.
    suspend fun getActiveMembership(
        campaignId: String,
        userId: String
    ): CampaignMembershipEntity? {
        return membershipDao.getActiveMembership(campaignId, userId)
    }

    // Retrieves the active Dungeon Master membership for a campaign.
    // Returns null if no active Dungeon Master membership is found.
    suspend fun getActiveDungeonMaster(
        campaignId: String
    ): CampaignMembershipEntity? {
        return membershipDao.getActiveDungeonMaster(campaignId)
    }

    // Checks whether a user currently has an active membership in a campaign.
    suspend fun isActiveMember(
        campaignId: String,
        userId: String
    ): Boolean {
        return membershipDao.isActiveMember(campaignId, userId)
    }

    // Retrieves the number of players who are currently active in a campaign.
    // Dungeon Masters and inactive members are not included in the count.
    suspend fun getActivePlayerCount(campaignId: String): Int {
        return membershipDao.getActivePlayerCount(campaignId)
    }

    // Allows a user to join an active campaign as a player.
    suspend fun joinCampaign(
        campaignId: String,
        userId: String
    ): CampaignMembershipEntity {

        // A transaction ensures that all database operations succeed together.
        // If an operation fails, the changes made during the transaction are rolled back.
        return database.withTransaction {

            // Checks that the campaign exists before attempting to join it.
            val campaign = campaignDao.getCampaignById(campaignId)
                ?: throw IllegalArgumentException("Campaign not found.")

            // Only active campaigns can accept new members.
            require(campaign.status == CampaignStatus.ACTIVE) {
                "This campaign is not currently accepting members."
            }

            // Checks that the user does not already have an active membership
            // in this campaign.
            require(
                membershipDao.getActiveMembership(campaignId, userId) == null
            ) {
                "You are already a member of this campaign."
            }

            // Checks whether the campaign has reached its maximum player limit.
            // Only active PLAYER memberships are included in the count.
            require(
                membershipDao.getActivePlayerCount(campaignId) < campaign.maxPlayers
            ) {
                "This campaign has reached its player limit."
            }

            // Creates a new membership with the PLAYER role.
            // A random UUID is used to generate a unique ID for the membership.
            val membership = CampaignMembershipEntity(
                id = UUID.randomUUID().toString(),
                campaignId = campaignId,
                userId = userId,
                role = CampaignRole.PLAYER,
                joinedAt = System.currentTimeMillis()
            )

            // Saves the new membership in the database.
            membershipDao.insertMembership(membership)

            // Returns the newly created membership.
            membership
        }
    }

    // Allows the current Dungeon Master to add another user to the campaign as a player.
    suspend fun addPlayerToCampaign(
        campaignId: String,
        dungeonMasterUserId: String,
        playerUserId: String
    ): CampaignMembershipEntity {

        // A transaction ensures that all database operations succeed together.
        // If an operation fails, the changes made during the transaction are rolled back.
        return database.withTransaction {

            // Checks that the campaign exists before attempting to add a player.
            val campaign = campaignDao.getCampaignById(campaignId)
                ?: throw IllegalArgumentException("Campaign not found.")

            // Players can only be added while the campaign is active.
            require(campaign.status == CampaignStatus.ACTIVE) {
                "Players can only be added to active campaigns."
            }

            // Retrieves the Dungeon Master's active membership in the campaign.
            val dungeonMasterMembership =
                membershipDao.getActiveMembership(
                    campaignId,
                    dungeonMasterUserId
                )

            // Checks that the user making the request is currently the Dungeon Master.
            require(dungeonMasterMembership?.role == CampaignRole.DM) {
                "Only the current Dungeon Master can add players."
            }

            // Prevents the Dungeon Master from adding themselves as a player.
            require(dungeonMasterUserId != playerUserId) {
                "The Dungeon Master cannot join their own campaign as a player."
            }

            // Checks that the player does not already have an active membership
            // in this campaign.
            require(
                membershipDao.getActiveMembership(campaignId, playerUserId) == null
            ) {
                "This profile is already a member of the campaign."
            }

            // Checks whether the campaign has reached its maximum player limit.
            require(
                membershipDao.getActivePlayerCount(campaignId) < campaign.maxPlayers
            ) {
                "This campaign has reached its player limit."
            }

            // Creates a new membership for the selected player.
            // The role is set to PLAYER because the Dungeon Master is adding
            // them as a regular campaign participant.
            val membership = CampaignMembershipEntity(
                id = UUID.randomUUID().toString(),
                campaignId = campaignId,
                userId = playerUserId,
                role = CampaignRole.PLAYER,
                joinedAt = System.currentTimeMillis()
            )

            // Saves the new membership in the database.
            membershipDao.insertMembership(membership)

            // Returns the newly created membership.
            membership
        }
    }

    // Removes a user's active membership from a campaign.
    // If the user is the Dungeon Master, the campaign is also suspended.
    suspend fun leaveCampaign(
        campaignId: String,
        userId: String
    ) {

        // A transaction ensures that the membership update and any campaign
        // status change succeed together.
        database.withTransaction {

            // Retrieves the user's active membership in the campaign.
            // Throws an exception if the user is not currently an active member.
            val membership = membershipDao.getActiveMembership(
                campaignId,
                userId
            ) ?: throw IllegalArgumentException(
                "You are not an active member of this campaign."
            )

            // Records the current time so it can be used for the membership
            // departure and any campaign status update.
            val currentTime = System.currentTimeMillis()

            // Marks the membership as inactive and records when the user left.
            // The membership record is kept so previous participation can be viewed.
            membershipDao.leaveCampaign(
                membershipId = membership.id,
                leftAt = currentTime
            )

            if (membership.role == CampaignRole.DM) {

                // Suspends the campaign when its Dungeon Master leaves.
                // This prevents the campaign from remaining active without a DM.
                campaignDao.updateCampaignStatus(
                    campaignId,
                    CampaignStatus.SUSPENDED,
                    currentTime
                )
            }
        }
    }
}
