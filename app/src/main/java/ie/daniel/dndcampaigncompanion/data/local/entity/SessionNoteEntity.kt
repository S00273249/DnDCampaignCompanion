package ie.daniel.dndcampaigncompanion.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents a note written by a user during a campaign session.
 *
 * This entity stores the note's content, the session it belongs to
 * and the user who created it. Each note also records when it was
 * created and last updated.
 */
@Entity(
    tableName = "session_notes",
    foreignKeys = [
        // Links the note to an existing session.
        // RESTRICT prevents a session from being deleted while notes reference it.
        ForeignKey(
            entity = SessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.RESTRICT
        ),

        // Links the note to the user who created it.
        // RESTRICT prevents a user profile from being deleted while their notes reference it.
        ForeignKey(
            entity = UserProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        // Indexes improve searches and filtering using these individual columns.
        Index("sessionId"),
        Index("userId"),

        // Improves queries that search using both the session ID and user ID.
        Index(value = ["sessionId", "userId"])
    ]
)
data class SessionNoteEntity(
    // Unique ID used to identify the note.
    @PrimaryKey
    val id: String,

    // ID of the session the note belongs to.
    val sessionId: String,

    // ID of the user who created the note.
    val userId: String,

    // Text content written by the user.
    val content: String,

    // Records when the note was originally created.
    val createdAt: Long,

    // Records when the note was last updated.
    val updatedAt: Long
)
