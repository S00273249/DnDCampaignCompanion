package ie.daniel.dndcampaigncompanion.data.repository

import androidx.room.withTransaction
import ie.daniel.dndcampaigncompanion.data.local.AppDatabase
import ie.daniel.dndcampaigncompanion.data.local.entity.CampaignRole
import ie.daniel.dndcampaigncompanion.data.local.entity.CampaignStatus
import ie.daniel.dndcampaigncompanion.data.local.entity.SessionEntity
import ie.daniel.dndcampaigncompanion.data.local.entity.SessionStatus
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * Handles campaign session operations.
 *
 * This repository manages the session lifecycle, allowing Dungeon Masters
 * to schedule, edit, start, complete, cancel and delete sessions.
 * It also provides methods for retrieving sessions and checking user permissions.
 */
class SessionRepository(
    private val database: AppDatabase
) {

    // DAOs used to access sessions, campaigns and campaign memberships.
    private val sessionDao = database.sessionDao()
    private val campaignDao = database.campaignDao()
    private val membershipDao = database.campaignMembershipDao()

    // Observes all sessions belonging to a campaign.
    // The Flow allows the UI to receive updates when session records change.
    fun getCampaignSessions(
        campaignId: String
    ): Flow<List<SessionEntity>> {
        return sessionDao.getCampaignSessions(campaignId)
    }

    // Observes planned sessions for a campaign.
    // This can be used to display upcoming sessions separately from completed or cancelled ones.
    fun getPlannedSessions(
        campaignId: String
    ): Flow<List<SessionEntity>> {
        return sessionDao.getPlannedSessions(campaignId)
    }

    // Retrieves a session using its unique ID.
    // Returns null if no matching session exists.
    suspend fun getSessionById(sessionId: String): SessionEntity? {
        return sessionDao.getSessionById(sessionId)
    }

    // Creates a new session for an active campaign.
    // Only the campaign's current Dungeon Master can schedule sessions.
    suspend fun createSession(
        campaignId: String,
        userId: String,
        title: String,
        scheduledAt: Long,
        description: String? = null
    ): SessionEntity {

        // Makes sure the session has a title before it is saved.
        require(title.isNotBlank()) {
            "Session title cannot be empty."
        }

        // Retrieves the campaign to check that it exists and is active.
        val campaign = campaignDao.getCampaignById(campaignId)
            ?: throw IllegalArgumentException("Campaign not found.")

        // Sessions can only be created for campaigns that are currently active.
        require(campaign.status == CampaignStatus.ACTIVE) {
            "Sessions cannot be created for an inactive campaign."
        }

        // Checks that the user has an active Dungeon Master membership.
        require(isDungeonMaster(campaignId, userId)) {
            "Only the Dungeon Master can schedule sessions."
        }

        // Uses the same timestamp for the session's creation and last update times.
        val currentTime = System.currentTimeMillis()

        // Creates a new session with a unique ID and an initial PLANNED status.
        val session = SessionEntity(
            id = UUID.randomUUID().toString(),
            campaignId = campaignId,
            title = title.trim(),
            description = description?.trim()?.takeIf { it.isNotEmpty() },
            scheduledAt = scheduledAt,
            status = SessionStatus.PLANNED,
            createdAt = currentTime,
            updatedAt = currentTime
        )

        // Saves the new session in the database.
        sessionDao.insertSession(session)

        // Returns the newly created session.
        return session
    }

    // Updates the details of an existing planned session.
    // Only the current Dungeon Master can edit sessions.
    suspend fun updateSession(
        session: SessionEntity,
        userId: String
    ) {

        // Checks that the user has an active Dungeon Master membership
        // in the campaign that owns this session.
        require(isDungeonMaster(session.campaignId, userId)) {
            "Only the Dungeon Master can edit sessions."
        }

        // Prevents sessions that have already started, completed or been cancelled
        // from having their scheduled details edited.
        require(session.status == SessionStatus.PLANNED) {
            "Only planned sessions can be edited."
        }

        // Makes sure the updated session still has a valid title.
        require(session.title.isNotBlank()) {
            "Session title cannot be empty."
        }

        // Creates an updated copy of the session.
        // The existing ID, campaign ID, status and creation timestamp are preserved.
        // The updated timestamp records when the changes were made.
        sessionDao.updateSession(
            session.copy(
                title = session.title.trim(),
                description = session.description?.trim()
                    ?.takeIf { it.isNotEmpty() },
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    // Starts a planned session.
    // Only the Dungeon Master can start a session, and only one session
    // can be in progress within a campaign at a time.
    suspend fun startSession(
        sessionId: String,
        userId: String
    ) {

        // A transaction ensures that the checks and status update happen together.
        database.withTransaction {

            // Retrieves the session that is being started.
            val session = sessionDao.getSessionById(sessionId)
                ?: throw IllegalArgumentException("Session not found.")

            // Checks that the user is the current Dungeon Master of this campaign.
            require(isDungeonMaster(session.campaignId, userId)) {
                "Only the Dungeon Master can start sessions."
            }

            // Retrieves the campaign to check that it still exists and is active.
            val campaign = campaignDao.getCampaignById(session.campaignId)
                ?: throw IllegalArgumentException("Campaign not found.")

            // Sessions cannot be started if their campaign is inactive or suspended.
            require(campaign.status == CampaignStatus.ACTIVE) {
                "Sessions cannot be started for an inactive campaign."
            }

            // Checks that there isn't already another active session in this campaign.
            require(sessionDao.getActiveSession(session.campaignId) == null) {
                "Another session is already in progress."
            }

            // Attempts to change the session status to IN_PROGRESS.
            // The DAO returns the number of rows affected by the update.
            val updatedRows = sessionDao.startSession(
                sessionId,
                System.currentTimeMillis()
            )

            // Confirms that exactly one session was successfully updated.
            check(updatedRows == 1) {
                "This session could not be started."
            }
        }
    }

    // Completes a session that is currently in progress.
    // Only the campaign's current Dungeon Master can complete it.
    suspend fun completeSession(
        sessionId: String,
        userId: String
    ) {

        // Retrieves the session to identify its campaign.
        val session = sessionDao.getSessionById(sessionId)
            ?: throw IllegalArgumentException("Session not found.")

        // Checks that the user has an active Dungeon Master membership.
        require(isDungeonMaster(session.campaignId, userId)) {
            "Only the Dungeon Master can complete sessions."
        }

        // Attempts to mark the session as completed and records the completion time.
        val updatedRows = sessionDao.completeSession(
            sessionId,
            System.currentTimeMillis()
        )

        // The DAO only updates sessions that are in progress.
        // If no row was updated, the session was not eligible for completion.
        check(updatedRows == 1) {
            "Only an in-progress session can be completed."
        }
    }

    // Cancels a planned session.
    // Only the campaign's current Dungeon Master can cancel it.
    suspend fun cancelSession(
        sessionId: String,
        userId: String
    ) {

        // Retrieves the session to identify its campaign.
        val session = sessionDao.getSessionById(sessionId)
            ?: throw IllegalArgumentException("Session not found.")

        // Checks that the user has an active Dungeon Master membership.
        require(isDungeonMaster(session.campaignId, userId)) {
            "Only the Dungeon Master can cancel sessions."
        }

        // Attempts to mark the session as cancelled and records the update time.
        val updatedRows = sessionDao.cancelSession(
            sessionId,
            System.currentTimeMillis()
        )

        // The DAO only updates planned sessions.
        // If no row was updated, the session was not eligible for cancellation.
        check(updatedRows == 1) {
            "Only planned sessions can be cancelled."
        }
    }

    // Permanently deletes a planned session.
    // Only the campaign's current Dungeon Master can delete sessions.
    suspend fun deletePlannedSession(
        sessionId: String,
        userId: String
    ) {

        // Retrieves the session to identify its campaign.
        val session = sessionDao.getSessionById(sessionId)
            ?: throw IllegalArgumentException("Session not found.")

        // Checks that the user has an active Dungeon Master membership.
        require(isDungeonMaster(session.campaignId, userId)) {
            "Only the Dungeon Master can delete sessions."
        }

        // Attempts to delete the session.
        val updatedRows = sessionDao.deletePlannedSession(sessionId)

        // The DAO only deletes planned sessions.
        // If no row was deleted, the session was not eligible for deletion.
        check(updatedRows == 1) {
            "Only planned sessions can be deleted."
        }
    }

    // Retrieves sessions scheduled within a specified time range.
    suspend fun getSessionsInTimeRange(
        startTime: Long,
        endTime: Long
    ): List<SessionEntity> {

        // Makes sure the supplied time range is valid.
        // The start time must not be later than the end time.
        require(startTime <= endTime) {
            "The start time must be before the end time."
        }

        return sessionDao.getSessionsInTimeRange(startTime, endTime)
    }

    // Checks whether a user is the active Dungeon Master of a campaign.
    // This is used by other repository methods to enforce session permissions.
    private suspend fun isDungeonMaster(
        campaignId: String,
        userId: String
    ): Boolean {

        // Retrieves the user's active membership in the campaign.
        val membership = membershipDao.getActiveMembership(
            campaignId,
            userId
        )

        // Returns true only when the user has an active DM membership.
        return membership?.role == CampaignRole.DM
    }
}
