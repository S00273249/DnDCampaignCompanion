package ie.daniel.dndcampaigncompanion.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents a campaign stored in the database.
 *
 * This entity stores the main campaign information, including its name,
 * description, setting, starting date, player limit and current status.
 * Each campaign is also linked to the user who created it.
 */
@Entity(
    tableName = "campaigns",
    foreignKeys = [
        // Links the campaign to the user who created it.
        // RESTRICT prevents the user profile from being deleted while campaigns reference it.
        ForeignKey(
            entity = UserProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["createdByUserId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        // Improves queries that search for campaigns using their creator's user ID.
        Index("createdByUserId")
    ]
)
data class CampaignEntity(
    // Unique ID used to identify the campaign.
    @PrimaryKey
    val id: String,

    // Name displayed when referring to the campaign.
    val name: String,

    // Optional description providing more information about the campaign.
    val description: String? = null,

    // Optional URI pointing to the campaign's cover image.
    val coverImageUri: String? = null,

    // Optional description of the campaign's world or setting.
    val setting: String? = null,

    // Optional location associated with the campaign.
    val location: String? = null,

    // Date the campaign is scheduled to begin, stored as a timestamp.
    val startingDate: Long,

    // Maximum number of players allowed in the campaign.
    val maxPlayers: Int,

    // ID of the user who originally created the campaign.
    val createdByUserId: String,

    // Current status of the campaign, stored using the CampaignStatus enum.
    val status: CampaignStatus,

    // Records when the campaign was originally created.
    val createdAt: Long,

    // Records when the campaign was last updated.
    val updatedAt: Long
)

/**
 * Represents the possible statuses of a campaign.
 *
 * These values indicate whether a campaign is currently active,
 * temporarily suspended or completed.
 */
enum class CampaignStatus {
    // The campaign is currently running.
    ACTIVE,

    // The campaign has been temporarily paused.
    SUSPENDED,

    // The campaign has finished.
    COMPLETED
}
