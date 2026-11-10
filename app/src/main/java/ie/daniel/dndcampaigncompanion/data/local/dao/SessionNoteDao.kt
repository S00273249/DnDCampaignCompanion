package ie.daniel.dndcampaigncompanion.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import ie.daniel.dndcampaigncompanion.data.local.entity.SessionNoteEntity
import kotlinx.coroutines.flow.Flow

/**
 * Handles database operations for session notes.
 *
 * This DAO allows users to create, update, retrieve and delete their notes.
 * Notes are associated with individual users and sessions, allowing users
 * to keep track of their own notes during different campaign sessions.
 */
@Dao
interface SessionNoteDao {

    // Inserts a new note into the database.
    // ABORT means the insert will fail if it conflicts with an existing record,
    // rather than replacing or ignoring the existing data.
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertNote(note: SessionNoteEntity)

    // Updates an existing note using its primary key.
    @Update
    suspend fun updateNote(note: SessionNoteEntity)

    // Retrieves a note using its unique ID.
    // Returns null if no matching note is found.
    @Query("SELECT * FROM session_notes WHERE id = :noteId")
    suspend fun getNoteById(noteId: String): SessionNoteEntity?

    // Retrieves all notes created by a specific user for a particular session.
    // Both the session ID and user ID must match.
    // Results are ordered by updatedAt in descending order,
    // so the most recently updated notes appear first.
    // Flow allows the UI to receive updates when the matching note records change.
    @Query("""
        SELECT * FROM session_notes
        WHERE sessionId = :sessionId
        AND userId = :userId
        ORDER BY updatedAt DESC
    """)
    fun getSessionNotes(
        sessionId: String,
        userId: String
    ): Flow<List<SessionNoteEntity>>

    // Retrieves all notes belonging to a specific user across all sessions.
    // Results are ordered by updatedAt in descending order,
    // so the most recently updated notes appear first.
    @Query("""
        SELECT * FROM session_notes
        WHERE userId = :userId
        ORDER BY updatedAt DESC
    """)
    fun getUserNotes(userId: String): Flow<List<SessionNoteEntity>>

    // Retrieves a specific note only if it belongs to the specified user.
    // Both the note ID and user ID must match.
    // Returns null if the note does not exist or belongs to another user.
    @Query("""
        SELECT * FROM session_notes
        WHERE id = :noteId
        AND userId = :userId
    """)
    suspend fun getUserNote(
        noteId: String,
        userId: String
    ): SessionNoteEntity?

    // Deletes a note only if it belongs to the specified user.
    // Both the note ID and user ID must match for the deletion to happen.
    // Returns the number of rows deleted, which will be 1 if successful or 0 if not.
    @Query("""
        DELETE FROM session_notes
        WHERE id = :noteId
        AND userId = :userId
    """)
    suspend fun deleteNote(
        noteId: String,
        userId: String
    ): Int

    // Checks whether a note exists and belongs to the specified user.
    // EXISTS returns true if a matching record is found, otherwise false.
    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM session_notes
            WHERE id = :noteId
            AND userId = :userId
        )
    """)
    suspend fun noteExists(
        noteId: String,
        userId: String
    ): Boolean
}
