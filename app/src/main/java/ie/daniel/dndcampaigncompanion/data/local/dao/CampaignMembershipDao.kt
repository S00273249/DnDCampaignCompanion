package ie.daniel.dndcampaigncompanion.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import ie.daniel.dndcampaigncompanion.data.local.entity.CampaignMembershipEntity
import ie.daniel.dndcampaigncompanion.data.local.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

/**
 * Handles database operations for campaign memberships.
 *
 * This DAO allows users to join and leave campaigns, retrieves membership
 * information and provides queries for finding active members, Dungeon Masters
 * and user profiles that are available to join a campaign.
 */
@Dao
interface CampaignMembershipDao {

    // Inserts a new membership record into the database.
    // ABORT means the insert will fail if it conflicts with an existing record,
    // rather than replacing or ignoring the existing data.
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertMembership(membership: CampaignMembershipEntity)

    // Updates an existing membership record using its primary key.
    @Update
    suspend fun updateMembership(membership: CampaignMembershipEntity)

    // Retrieves a membership record using its unique ID.
    // Returns null if no matching membership is found.
    @Query("SELECT * FROM campaign_memberships WHERE id = :membershipId")
    suspend fun getMembershipById(membershipId: String): CampaignMembershipEntity?

    // Finds the active membership for a specific user in a campaign.
    // Both the campaign ID and user ID must match, and inactive memberships are ignored.
    // LIMIT 1 ensures that only one matching record is returned.
    @Query("""
        SELECT * FROM campaign_memberships
        WHERE campaignId = :campaignId
        AND userId = :userId
        AND isActive = 1
        LIMIT 1
    """)
    suspend fun getActiveMembership(
        campaignId: String,
        userId: String
    ): CampaignMembershipEntity?

    // Retrieves all active members belonging to a campaign.
    // Results are ordered by joinedAt, with the earliest members appearing first.
    // Flow allows the UI to receive updates when the matching membership records change.
    @Query("""
        SELECT * FROM campaign_memberships
        WHERE campaignId = :campaignId
        AND isActive = 1
        ORDER BY joinedAt ASC
    """)
    fun getActiveCampaignMembers(
        campaignId: String
    ): Flow<List<CampaignMembershipEntity>>

    // Retrieves the complete membership history for a campaign.
    // This includes both active and inactive memberships.
    // The most recently joined members appear first.
    @Query("""
        SELECT * FROM campaign_memberships
        WHERE campaignId = :campaignId
        ORDER BY joinedAt DESC
    """)
    fun getCampaignMembershipHistory(
        campaignId: String
    ): Flow<List<CampaignMembershipEntity>>

    // Retrieves all active campaign memberships belonging to a user.
    // This can be used to find which campaigns a user currently belongs to.
    // The most recently joined campaigns appear first.
    @Query("""
        SELECT * FROM campaign_memberships
        WHERE userId = :userId
        AND isActive = 1
        ORDER BY joinedAt DESC
    """)
    fun getUserActiveMemberships(
        userId: String
    ): Flow<List<CampaignMembershipEntity>>

    // Finds the active Dungeon Master membership for a campaign.
    // The role must be DM and the membership must still be active.
    // LIMIT 1 ensures that only one matching record is returned.
    @Query("""
        SELECT * FROM campaign_memberships
        WHERE campaignId = :campaignId
        AND role = 'DM'
        AND isActive = 1
        LIMIT 1
    """)
    suspend fun getActiveDungeonMaster(
        campaignId: String
    ): CampaignMembershipEntity?

    // Retrieves user profiles that are not already active members of a campaign.
    // The subquery first finds the user IDs of all active campaign members.
    // NOT IN then excludes those users from the returned profiles.
    // COLLATE NOCASE makes the username sorting case-insensitive.
    // For example, "Alice" and "alice" are treated as equal when sorting.
    @Query("""
        SELECT * FROM user_profiles
        WHERE id NOT IN (
            SELECT userId FROM campaign_memberships
            WHERE campaignId = :campaignId
            AND isActive = 1
        )
        ORDER BY username COLLATE NOCASE ASC
    """)
    fun getProfilesAvailableForCampaign(
        campaignId: String
    ): Flow<List<UserProfileEntity>>

    // Marks a membership as inactive when a user leaves a campaign.
    // The leftAt field records when the user left.
    // The membership record is kept so that previous participation can be viewed.
    @Query("""
        UPDATE campaign_memberships
        SET isActive = 0, leftAt = :leftAt
        WHERE id = :membershipId
    """)
    suspend fun leaveCampaign(
        membershipId: String,
        leftAt: Long
    )

    // Checks whether a user currently has an active membership in a campaign.
    // EXISTS returns true if a matching record is found, otherwise false.
    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM campaign_memberships
            WHERE campaignId = :campaignId
            AND userId = :userId
            AND isActive = 1
        )
    """)
    suspend fun isActiveMember(
        campaignId: String,
        userId: String
    ): Boolean

    // Counts the number of active players in a campaign.
    // Only memberships with the PLAYER role are included.
    // Dungeon Masters and inactive members are excluded from the count.
    @Query("""
        SELECT COUNT(*) FROM campaign_memberships
        WHERE campaignId = :campaignId
        AND isActive = 1
        AND role = 'PLAYER'
    """)
    suspend fun getActivePlayerCount(
        campaignId: String
    ): Int
}
