package ie.daniel.dndcampaigncompanion.data.repository

import ie.daniel.dndcampaigncompanion.data.local.dao.SessionNoteDao
import ie.daniel.dndcampaigncompanion.data.local.dao.SessionDao
import ie.daniel.dndcampaigncompanion.data.local.dao.CampaignMembershipDao
import ie.daniel.dndcampaigncompanion.data.local.entity.SessionNoteEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * Handles session note operations.
 *
 * This repository allows campaign members to create and manage their own notes
 * for individual sessions. It also checks campaign membership when creating
 * notes and ensures users can only update or delete their own notes.
 */
class SessionNoteRepository(
    private val sessionNoteDao: SessionNoteDao,
    private val sessionDao: SessionDao,
    private val membershipDao: CampaignMembershipDao
) {

    // Observes the notes created by a specific user for a particular session.
    // The Flow allows the UI to receive updates when notes are added, edited or deleted.
    fun getSessionNotes(
        sessionId: String,
        userId: String
    ): Flow<List<SessionNoteEntity>> {
        return sessionNoteDao.getSessionNotes(sessionId, userId)
    }

    // Observes all session notes belonging to a specific user across their sessions.
    fun getUserNotes(userId: String): Flow<List<SessionNoteEntity>> {
        return sessionNoteDao.getUserNotes(userId)
    }

    // Retrieves a specific note only if it belongs to the specified user.
    // Returns null if the note does not exist or belongs to someone else.
    suspend fun getNoteById(
        noteId: String,
        userId: String
    ): SessionNoteEntity? {
        return sessionNoteDao.getUserNote(noteId, userId)
    }

    // Creates a new note for a particular session.
    suspend fun createNote(
        sessionId: String,
        userId: String,
        content: String
    ): SessionNoteEntity {

        // Makes sure the note contains some text before it is saved.
        require(content.isNotBlank()) {
            "Note content cannot be empty."
        }

        // Retrieves the session so its campaign ID can be used to check membership.
        // Throws an exception if the session does not exist.
        val session = sessionDao.getSessionById(sessionId)
            ?: throw IllegalArgumentException("Session not found.")

        // Only users with an active membership in the session's campaign
        // are allowed to create notes for that session.
        require(
            membershipDao.isActiveMember(session.campaignId, userId)
        ) {
            "You must be an active campaign member to create session notes."
        }

        // Uses the same timestamp for the note's creation and last update times.
        val currentTime = System.currentTimeMillis()

        // Creates a new note with a unique ID and the supplied content.
        val note = SessionNoteEntity(
            id = UUID.randomUUID().toString(),
            sessionId = sessionId,
            userId = userId,
            content = content.trim(),
            createdAt = currentTime,
            updatedAt = currentTime
        )

        // Saves the new note in the database.
        sessionNoteDao.insertNote(note)

        // Returns the newly created note.
        return note
    }

    // Updates the content of an existing note.
    suspend fun updateNote(
        noteId: String,
        userId: String,
        content: String
    ) {

        // Makes sure the updated note still contains some text.
        require(content.isNotBlank()) {
            "Note content cannot be empty."
        }

        // Retrieves the note using both its ID and the user's ID.
        // This ensures that users can only update notes they own.
        val existingNote = sessionNoteDao.getUserNote(noteId, userId)
            ?: throw IllegalArgumentException(
                "Note not found or does not belong to this user."
            )

        // Creates an updated copy of the note.
        // The original ID, session ID and creation timestamp are preserved.
        // The updated timestamp records when the changes were made.
        sessionNoteDao.updateNote(
            existingNote.copy(
                content = content.trim(),
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    // Deletes a note belonging to the specified user.
    suspend fun deleteNote(
        noteId: String,
        userId: String
    ) {

        // Attempts to delete the note using both its ID and the user's ID.
        // The DAO returns the number of database rows that were deleted.
        val deletedRows = sessionNoteDao.deleteNote(noteId, userId)

        // Checks that exactly one note was deleted.
        // Throws an exception if the note was not found or did not belong to the user.
        check(deletedRows == 1) {
            "Note not found or does not belong to this user."
        }
    }
}
