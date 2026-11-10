package ie.daniel.dndcampaigncompanion.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import ie.daniel.dndcampaigncompanion.data.local.entity.CampaignPhotoEntity
import kotlinx.coroutines.flow.Flow

/**
 * Handles database operations for campaign photos.
 *
 * This DAO allows photos to be added, updated, retrieved and deleted.
 * It also provides queries for retrieving photos belonging to a campaign
 * or a specific user, checking photo ownership and counting campaign photos.
 */
@Dao
interface CampaignPhotoDao {

    // Inserts a new photo record into the database.
    // ABORT means the insert will fail if it conflicts with an existing record,
    // rather than replacing or ignoring the existing data.
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPhoto(photo: CampaignPhotoEntity)

    // Updates an existing photo record using its primary key.
    @Update
    suspend fun updatePhoto(photo: CampaignPhotoEntity)

    // Retrieves a photo using its unique ID.
    // Returns null if no matching photo is found.
    @Query("SELECT * FROM campaign_photos WHERE id = :photoId")
    suspend fun getPhotoById(photoId: String): CampaignPhotoEntity?

    // Retrieves all photos belonging to a specific campaign.
    // Results are ordered by capturedAt in descending order,
    // so the most recently captured photos appear first.
    // Flow allows the UI to receive updates when the matching photo records change.
    @Query("""
        SELECT * FROM campaign_photos
        WHERE campaignId = :campaignId
        ORDER BY capturedAt DESC
    """)
    fun getCampaignPhotos(
        campaignId: String
    ): Flow<List<CampaignPhotoEntity>>

    // Retrieves photos from a campaign that were uploaded by a specific user.
    // Both the campaign ID and uploader's user ID must match.
    // The most recently captured photos appear first.
    @Query("""
        SELECT * FROM campaign_photos
        WHERE campaignId = :campaignId
        AND uploadedByUserId = :userId
        ORDER BY capturedAt DESC
    """)
    fun getUserCampaignPhotos(
        campaignId: String,
        userId: String
    ): Flow<List<CampaignPhotoEntity>>

    // Deletes a photo only if it belongs to the specified user.
    // Both the photo ID and uploader's user ID must match for the deletion to happen.
    // Returns the number of rows deleted, which will be 1 if successful or 0 if not.
    @Query("""
        DELETE FROM campaign_photos
        WHERE id = :photoId
        AND uploadedByUserId = :userId
    """)
    suspend fun deleteOwnPhoto(
        photoId: String,
        userId: String
    ): Int

    // Deletes a photo using its ID without checking who uploaded it.
    // This allows a Dungeon Master to delete any photo in the campaign.
    // Returns the number of rows deleted, which will be 1 if successful or 0 if not.
    @Query("""
        DELETE FROM campaign_photos
        WHERE id = :photoId
    """)
    suspend fun deletePhotoAsDungeonMaster(
        photoId: String
    ): Int

    // Checks whether a specific user uploaded a photo.
    // EXISTS returns true if the photo ID and uploader's user ID match,
    // otherwise it returns false.
    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM campaign_photos
            WHERE id = :photoId
            AND uploadedByUserId = :userId
        )
    """)
    suspend fun isPhotoUploadedByUser(
        photoId: String,
        userId: String
    ): Boolean

    // Counts the total number of photos belonging to a campaign.
    @Query("""
        SELECT COUNT(*) FROM campaign_photos
        WHERE campaignId = :campaignId
    """)
    suspend fun getCampaignPhotoCount(
        campaignId: String
    ): Int
}
