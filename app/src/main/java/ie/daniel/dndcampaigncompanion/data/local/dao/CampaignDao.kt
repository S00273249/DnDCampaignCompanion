package ie.daniel.dndcampaigncompanion.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import ie.daniel.dndcampaigncompanion.data.local.entity.CampaignEntity
import ie.daniel.dndcampaigncompanion.data.local.entity.CampaignStatus
import kotlinx.coroutines.flow.Flow

/**
 * Handles database operations for campaigns.
 *
 * This DAO allows campaigns to be created, updated and retrieved.
 * It also provides queries for finding campaigns belonging to a user,
 * checking whether a campaign exists and updating its status.
 */
@Dao
interface CampaignDao {

    // Inserts a new campaign into the database.
    // ABORT means the insert will fail if it conflicts with an existing record,
    // rather than replacing or ignoring the existing data.
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertCampaign(campaign: CampaignEntity)

    // Updates an existing campaign using its primary key.
    @Update
    suspend fun updateCampaign(campaign: CampaignEntity)

    // Retrieves a campaign using its unique ID.
    // Returns null if no matching campaign is found.
    @Query("SELECT * FROM campaigns WHERE id = :campaignId")
    suspend fun getCampaignById(campaignId: String): CampaignEntity?

    // Observes a campaign using its unique ID.
    // Unlike getCampaignById(), this returns a Flow that emits the campaign again
    // whenever the matching database record changes.
    @Query("SELECT * FROM campaigns WHERE id = :campaignId")
    fun observeCampaignById(campaignId: String): Flow<CampaignEntity?>

    // Retrieves all campaigns, with the most recently created campaigns appearing first.
    // Flow allows the UI to receive updates when campaigns are added or changed.
    @Query("SELECT * FROM campaigns ORDER BY createdAt DESC")
    fun getAllCampaigns(): Flow<List<CampaignEntity>>

    // Retrieves campaigns created by a specific user.
    // createdByUserId is used to match the user who originally created each campaign.
    // Results are ordered with the newest campaigns first.
    @Query("SELECT * FROM campaigns WHERE createdByUserId = :userId ORDER BY createdAt DESC")
    fun getCampaignsCreatedByUser(userId: String): Flow<List<CampaignEntity>>

    // Retrieves campaigns that a user is currently an active member of.
    // INNER JOIN connects campaigns to their membership records using the campaign IDs.
    // Both the user ID and active membership status must match.
    // For example, a campaign will only be returned if the user has an active
    // membership record for that campaign.
    @Query("""
        SELECT campaigns.*
        FROM campaigns
        INNER JOIN campaign_memberships
        ON campaigns.id = campaign_memberships.campaignId
        WHERE campaign_memberships.userId = :userId
        AND campaign_memberships.isActive = 1
        ORDER BY campaigns.createdAt DESC
    """)
    fun getCampaignsForUser(userId: String): Flow<List<CampaignEntity>>

    // Updates the status of a campaign and records when it was last updated.
    // The campaign is identified using its unique ID.
    @Query("UPDATE campaigns SET status = :status, updatedAt = :updatedAt WHERE id = :campaignId")
    suspend fun updateCampaignStatus(
        campaignId: String,
        status: CampaignStatus,
        updatedAt: Long
    )

    // Checks whether a campaign exists in the database.
    // EXISTS returns true if a matching campaign is found, otherwise false.
    @Query("SELECT EXISTS(SELECT 1 FROM campaigns WHERE id = :campaignId)")
    suspend fun campaignExists(campaignId: String): Boolean
}
