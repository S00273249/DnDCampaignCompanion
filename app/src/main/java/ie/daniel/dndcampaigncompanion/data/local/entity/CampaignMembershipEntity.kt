package ie.daniel.dndcampaigncompanion.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents a user's membership in a campaign.
 *
 * This entity links a user profile to a campaign and stores their role,
 * joining date and current membership status. When a user leaves a campaign,
 * their membership can be marked as inactive rather than being deleted.
 * This allows previous membership history to be retained.
 */
@Entity(
    tableName = "campaign_memberships",
    foreignKeys = [
        // Links the membership to an existing campaign.
        // RESTRICT prevents a campaign from being deleted while membership records reference it.
        ForeignKey(
            entity = CampaignEntity::class,
            parentColumns = ["id"],
            childColumns = ["campaignId"],
            onDelete = ForeignKey.RESTRICT
        ),

        // Links the membership to an existing user profile.
        // RESTRICT prevents a user profile from being deleted while membership records reference it.
        ForeignKey(
            entity = UserProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        // Indexes improve searches and filtering using these individual columns.
        Index("campaignId"),
        Index("userId"),

        // Improves queries that search using both the campaign ID and user ID.
        // unique = false means multiple records with the same combination are allowed.
        Index(
            value = ["campaignId", "userId"],
            unique = false
        )
    ]
)
data class CampaignMembershipEntity(
    // Unique ID for this membership record.
    @PrimaryKey
    val id: String,

    // ID of the campaign the user belongs to.
    val campaignId: String,

    // ID of the user who is a member of the campaign.
    val userId: String,

    // Determines whether the user is a Dungeon Master or a regular player.
    val role: CampaignRole,

    // Records when the user joined the campaign.
    val joinedAt: Long,

    // Records when the user left the campaign.
    // Remains null while the membership is active.
    val leftAt: Long? = null,

    // Tracks whether the user is currently a member of the campaign.
    // New memberships are active by default.
    // Setting this to false allows the membership history to be kept in the database.
    val isActive: Boolean = true
)

/**
 * Represents the possible roles a user can have within a campaign.
 */
enum class CampaignRole {
    // The Dungeon Master manages the campaign and runs its sessions.
    DM,

    // A player participates in the campaign and controls a character.
    PLAYER
}
