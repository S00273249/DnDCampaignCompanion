package ie.daniel.dndcampaigncompanion.data.repository

import ie.daniel.dndcampaigncompanion.data.local.dao.CampaignPhotoDao
import ie.daniel.dndcampaigncompanion.data.local.dao.CampaignMembershipDao
import ie.daniel.dndcampaigncompanion.data.local.entity.CampaignPhotoEntity
import ie.daniel.dndcampaigncompanion.data.local.entity.CampaignRole
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * Handles photo operations for campaigns.
 *
 * This repository allows campaign members to upload photos, update captions
 * and delete photos. It also checks membership and permissions to make sure
 * users can only perform actions they are allowed to.
 */
class CampaignPhotoRepository(
    private val campaignPhotoDao: CampaignPhotoDao,
    private val membershipDao: CampaignMembershipDao
) {

    // Retrieves all photos belonging to a campaign.
    // The Flow allows the UI to receive updates when photos are added or removed.
    fun getCampaignPhotos(
        campaignId: String
    ): Flow<List<CampaignPhotoEntity>> {
        return campaignPhotoDao.getCampaignPhotos(campaignId)
    }

    // Retrieves only the photos uploaded by a specific user within a campaign.
    fun getUserCampaignPhotos(
        campaignId: String,
        userId: String
    ): Flow<List<CampaignPhotoEntity>> {
        return campaignPhotoDao.getUserCampaignPhotos(
            campaignId,
            userId
        )
    }

    // Retrieves a photo using its unique ID.
    // Returns null if no matching photo exists.
    suspend fun getPhotoById(photoId: String): CampaignPhotoEntity? {
        return campaignPhotoDao.getPhotoById(photoId)
    }

    // Uploads a photo to a campaign.
    suspend fun uploadPhoto(
        campaignId: String,
        userId: String,
        imageUri: String,
        caption: String? = null
    ): CampaignPhotoEntity {

        // Makes sure an image URI has been provided.
        // A blank URI would not identify an image that can be displayed.
        require(imageUri.isNotBlank()) {
            "An image is required."
        }

        // Only users with an active membership in the campaign can upload photos.
        require(membershipDao.isActiveMember(campaignId, userId)) {
            "You must be an active campaign member to upload photos."
        }

        // Creates a new photo record with a unique ID and the uploader's details.
        val photo = CampaignPhotoEntity(
            id = UUID.randomUUID().toString(),
            campaignId = campaignId,
            uploadedByUserId = userId,
            imageUri = imageUri,

            // Removes unnecessary whitespace from the caption.
            // If the caption is null or empty after trimming, it is stored as null.
            caption = caption?.trim()?.takeIf { it.isNotEmpty() },

            // Records when the photo was uploaded.
            capturedAt = System.currentTimeMillis()
        )

        // Saves the photo record in the database.
        campaignPhotoDao.insertPhoto(photo)

        // Returns the newly created photo.
        return photo
    }

    // Updates the caption of an existing campaign photo.
    suspend fun updatePhotoCaption(
        photoId: String,
        userId: String,
        caption: String?
    ) {

        // Retrieves the photo before attempting to update it.
        val photo = campaignPhotoDao.getPhotoById(photoId)
            ?: throw IllegalArgumentException("Photo not found.")

        // Checks that the user currently belongs to the campaign containing the photo.
        // Inactive members cannot edit campaign photos.
        val membership = membershipDao.getActiveMembership(
            photo.campaignId,
            userId
        ) ?: throw IllegalArgumentException(
            "You must be an active campaign member."
        )

        // Allows the original uploader or the campaign's Dungeon Master
        // to edit the photo caption.
        require(
            photo.uploadedByUserId == userId ||
                    membership.role == CampaignRole.DM
        ) {
            "You do not have permission to edit this photo."
        }

        // Creates an updated copy of the photo with the new caption.
        // The original photo details, such as its ID and image URI, are preserved.
        campaignPhotoDao.updatePhoto(
            photo.copy(
                // Trims whitespace and stores null if the caption is empty.
                caption = caption?.trim()?.takeIf { it.isNotEmpty() }
            )
        )
    }

    // Deletes an existing campaign photo.
    suspend fun deletePhoto(
        photoId: String,
        userId: String
    ) {

        // Retrieves the photo before attempting to delete it.
        val photo = campaignPhotoDao.getPhotoById(photoId)
            ?: throw IllegalArgumentException("Photo not found.")

        // Checks that the user currently belongs to the campaign containing the photo.
        val membership = membershipDao.getActiveMembership(
            photo.campaignId,
            userId
        ) ?: throw IllegalArgumentException(
            "You must be an active campaign member."
        )

        // Determines which delete operation the user is allowed to perform.
        val deletedRows = when {
            // The original uploader can delete their own photo.
            photo.uploadedByUserId == userId ->
                campaignPhotoDao.deleteOwnPhoto(photoId, userId)

            // The Dungeon Master can also delete photos uploaded by other members.
            membership.role == CampaignRole.DM ->
                campaignPhotoDao.deletePhotoAsDungeonMaster(photoId)

            // Other campaign members cannot delete someone else's photo.
            else -> throw IllegalArgumentException(
                "You do not have permission to delete this photo."
            )
        }

        // Checks that exactly one database record was deleted.
        // If no record was deleted, or more than one was affected, an exception is thrown.
        check(deletedRows == 1) {
            "The photo could not be deleted."
        }
    }

    // Retrieves the total number of photos stored for a campaign.
    suspend fun getCampaignPhotoCount(campaignId: String): Int {
        return campaignPhotoDao.getCampaignPhotoCount(campaignId)
    }
}
