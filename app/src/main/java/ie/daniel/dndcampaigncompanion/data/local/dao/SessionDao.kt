package ie.daniel.dndcampaigncompanion.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import ie.daniel.dndcampaigncompanion.data.local.entity.SessionEntity
import ie.daniel.dndcampaigncompanion.data.local.entity.SessionStatus
import kotlinx.coroutines.flow.Flow

/**
 * Handles database operations for campaign sessions.
 *
 * This DAO allows sessions to be created, updated and retrieved.
 * It also manages session status changes, including starting, completing
 * and cancelling sessions, as well as deleting planned sessions.
 */
@Dao
interface SessionDao {

    // Inserts a new session into the database.
    // ABORT means the insert will fail if it conflicts with an existing record,
    // rather than replacing or ignoring the existing data.
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSession(session: SessionEntity)

    // Updates an existing session using its primary key.
    @Update
    suspend fun updateSession(session: SessionEntity)

    // Retrieves a session using its unique ID.
    // Returns null if no matching session is found.
    @Query("SELECT * FROM sessions WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: String): SessionEntity?

    // Observes a session using its unique ID.
    // Unlike getSessionById(), this returns a Flow that emits the session again
    // whenever the matching database record changes.
    @Query("SELECT * FROM sessions WHERE id = :sessionId")
    fun observeSessionById(sessionId: String): Flow<SessionEntity?>

    // Retrieves all sessions belonging to a specific campaign.
    // Results are ordered by scheduledAt in descending order,
    // so the latest scheduled sessions appear first.
    // Flow allows the UI to receive updates when session records change.
    @Query("""
        SELECT * FROM sessions
        WHERE campaignId = :campaignId
        ORDER BY scheduledAt DESC
    """)
    fun getCampaignSessions(
        campaignId: String
    ): Flow<List<SessionEntity>>

    // Retrieves sessions that are planned but have not started yet.
    // Results are ordered by scheduledAt in ascending order,
    // so the next upcoming session appears first.
    @Query("""
        SELECT * FROM sessions
        WHERE campaignId = :campaignId
        AND status = 'PLANNED'
        ORDER BY scheduledAt ASC
    """)
    fun getPlannedSessions(
        campaignId: String
    ): Flow<List<SessionEntity>>

    // Finds a session that is currently in progress within a campaign.
    // LIMIT 1 ensures that only one matching session is returned.
    @Query("""
        SELECT * FROM sessions
        WHERE campaignId = :campaignId
        AND status = 'IN_PROGRESS'
        LIMIT 1
    """)
    suspend fun getActiveSession(
        campaignId: String
    ): SessionEntity?

    // Starts a session by changing its status from PLANNED to IN_PROGRESS.
    // The start time is recorded in startedAt and updatedAt.
    // The status condition prevents a session that has already started,
    // completed or been cancelled from being started again.
    // Returns the number of rows updated, which will be 1 if successful or 0 if not.
    @Query("""
        UPDATE sessions
        SET status = 'IN_PROGRESS',
            startedAt = :startedAt,
            updatedAt = :startedAt
        WHERE id = :sessionId
        AND status = 'PLANNED'
    """)
    suspend fun startSession(
        sessionId: String,
        startedAt: Long
    ): Int

    // Completes a session by changing its status from IN_PROGRESS to COMPLETED.
    // The completion time is recorded in completedAt and updatedAt.
    // The status condition prevents a session that has not started or has
    // already finished from being completed again.
    // Returns the number of rows updated, which will be 1 if successful or 0 if not.
    @Query("""
        UPDATE sessions
        SET status = 'COMPLETED',
            completedAt = :completedAt,
            updatedAt = :completedAt
        WHERE id = :sessionId
        AND status = 'IN_PROGRESS'
    """)
    suspend fun completeSession(
        sessionId: String,
        completedAt: Long
    ): Int

    // Cancels a session by changing its status from PLANNED to CANCELLED.
    // The updatedAt field records when the cancellation happened.
    // Only planned sessions can be cancelled using this query.
    // Returns the number of rows updated, which will be 1 if successful or 0 if not.
    @Query("""
        UPDATE sessions
        SET status = 'CANCELLED',
            updatedAt = :updatedAt
        WHERE id = :sessionId
        AND status = 'PLANNED'
    """)
    suspend fun cancelSession(
        sessionId: String,
        updatedAt: Long
    ): Int

    // Deletes a session only if it is still in the PLANNED state.
    // Sessions that have started or finished cannot be deleted using this query.
    // Returns the number of rows deleted, which will be 1 if successful or 0 if not.
    @Query("""
        DELETE FROM sessions
        WHERE id = :sessionId
        AND status = 'PLANNED'
    """)
    suspend fun deletePlannedSession(
        sessionId: String
    ): Int

    // Retrieves planned sessions scheduled within the specified time range.
    // BETWEEN includes sessions scheduled exactly at either boundary.
    // Results are ordered by scheduledAt in ascending order.
    // For example, this can be used to find upcoming sessions within a calendar period.
    @Query("""
        SELECT * FROM sessions
        WHERE status = 'PLANNED'
        AND scheduledAt BETWEEN :startTime AND :endTime
        ORDER BY scheduledAt ASC
    """)
    suspend fun getSessionsInTimeRange(
        startTime: Long,
        endTime: Long
    ): List<SessionEntity>

    // Checks whether a campaign has at least one session with the specified status.
    // The status is passed into the query, so this can check for PLANNED,
    // IN_PROGRESS, COMPLETED or CANCELLED sessions.
    // EXISTS returns true if a matching session is found, otherwise false.
    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM sessions
            WHERE campaignId = :campaignId
            AND status = :status
        )
    """)
    suspend fun campaignHasSessionWithStatus(
        campaignId: String,
        status: SessionStatus
    ): Boolean
}
