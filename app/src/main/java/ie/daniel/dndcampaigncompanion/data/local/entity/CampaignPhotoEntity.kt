package ie.daniel.dndcampaigncompanion.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents a photo uploaded to a campaign.
 *
 * This entity stores the photo's location, optional caption, capture date
 * and the user who uploaded it. Each photo is linked to a campaign so
 * that campaign members can retrieve and view its shared photos.
 */
@Entity(
    tableName = "campaign_photos",
    foreignKeys = [
        // Links the photo to an existing campaign.
        // RESTRICT prevents a campaign from being deleted while photos reference it.
        ForeignKey(
            entity = CampaignEntity::class,
            parentColumns = ["id"],
            childColumns = ["campaignId"],
            onDelete = ForeignKey.RESTRICT
        ),

        // Links the photo to the user who uploaded it.
        // RESTRICT prevents a user profile from being deleted while their photos reference it.
        ForeignKey(
            entity = UserProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["uploadedByUserId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        // Indexes improve searches and filtering using these individual columns.
        Index("campaignId"),
        Index("uploadedByUserId"),

        // Improves queries that filter by campaign and sort by capture date.
        Index(value = ["campaignId", "capturedAt"])
    ]
)
data class CampaignPhotoEntity(
    // Unique ID used to identify the photo.
    @PrimaryKey
    val id: String,

    // ID of the campaign the photo belongs to.
    val campaignId: String,

    // ID of the user who uploaded the photo.
    val uploadedByUserId: String,

    // URI pointing to the image stored on the device.
    val imageUri: String,

    // Optional caption displayed alongside the photo.
    val caption: String? = null,

    // Records when the photo was captured, stored as a timestamp.
    val capturedAt: Long
)
