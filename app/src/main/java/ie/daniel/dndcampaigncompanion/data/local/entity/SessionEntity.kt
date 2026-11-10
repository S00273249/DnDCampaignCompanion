package ie.daniel.dndcampaigncompanion.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents a session belonging to a campaign.
 *
 * This entity stores the session's title, description, scheduled date
 * and timestamps for when it starts and finishes. It also tracks the
 * current status of the session throughout its lifecycle.
 */
@Entity(
    tableName = "sessions",
    foreignKeys = [
        // Links the session to an existing campaign.
        // RESTRICT prevents a campaign from being deleted while sessions reference it.
        ForeignKey(
            entity = CampaignEntity::class,
            parentColumns = ["id"],
            childColumns = ["campaignId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        // Improves queries that search for sessions using their campaign ID.
        Index("campaignId")
    ]
)
data class SessionEntity(
    // Unique ID used to identify the session.
    @PrimaryKey
    val id: String,

    // ID of the campaign the session belongs to.
    val campaignId: String,

    // Title displayed when referring to the session.
    val title: String,

    // Optional description providing more information about the session.
    val description: String? = null,

    // Date and time the session is scheduled to take place, stored as a timestamp.
    val scheduledAt: Long,

    // Records when the session actually started.
    // Remains null until the session begins.
    val startedAt: Long? = null,

    // Records when the session was completed.
    // Remains null until the session finishes.
    val completedAt: Long? = null,

    // Current status of the session, stored using the SessionStatus enum.
    val status: SessionStatus,

    // Records when the session was originally created.
    val createdAt: Long,

    // Records when the session was last updated.
    val updatedAt: Long
)

/**
 * Represents the possible statuses of a campaign session.
 *
 * These values track the session from its initial planning through
 * to its completion or cancellation.
 */
enum class SessionStatus {
    // The session has been scheduled but has not started yet.
    PLANNED,

    // The session is currently taking place.
    IN_PROGRESS,

    // The session has finished.
    COMPLETED,

    // The session was cancelled before it took place.
    CANCELLED
}
