package ie.daniel.dndcampaigncompanion.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import ie.daniel.dndcampaigncompanion.data.local.entity.CampaignCharacterEntity
import kotlinx.coroutines.flow.Flow

/**
 * Handles database operations for campaign character assignments.
 *
 * This DAO allows characters to be assigned to campaigns, updated, retrieved
 * and moved between active and inactive status. It also provides queries for
 * checking a user's current assignment and viewing character participation history.
 */
@Dao
interface CampaignCharacterDao {

    // Inserts a new character assignment into the database.
    // ABORT means the insert will fail if it conflicts with an existing record,
    // rather than replacing or ignoring the existing data.
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertCampaignCharacter(
        campaignCharacter: CampaignCharacterEntity
    )

    // Updates an existing character assignment using its primary key.
    @Update
    suspend fun updateCampaignCharacter(
        campaignCharacter: CampaignCharacterEntity
    )

    // Retrieves a campaign character assignment using its unique ID.
    // Returns null if no matching assignment is found.
    @Query("SELECT * FROM campaign_characters WHERE id = :id")
    suspend fun getCampaignCharacterById(
        id: String
    ): CampaignCharacterEntity?

    // Retrieves all active character assignments belonging to a campaign.
    // isActive = 1 means the assignment is currently active.
    // Results are ordered by joinedAt, with the oldest assignments appearing first.
    // Flow allows the UI to receive updates when the matching database records change.
    @Query("""
        SELECT * FROM campaign_characters
        WHERE campaignId = :campaignId
        AND isActive = 1
        ORDER BY joinedAt ASC
    """)
    fun getActiveCampaignCharacters(
        campaignId: String
    ): Flow<List<CampaignCharacterEntity>>

    // Finds the active character assignment for a specific user in a campaign.
    // Both campaignId and userId must match, and inactive assignments are ignored.
    // LIMIT 1 ensures that only one matching record is returned.
    @Query("""
        SELECT * FROM campaign_characters
        WHERE campaignId = :campaignId
        AND userId = :userId
        AND isActive = 1
        LIMIT 1
    """)
    suspend fun getActiveCharacterForUser(
        campaignId: String,
        userId: String
    ): CampaignCharacterEntity?

    // Retrieves the history of a specific character within a particular campaign.
    // Results are ordered by joinedAt in descending order, so the most recent
    // assignment appears first.
    @Query("""
        SELECT * FROM campaign_characters
        WHERE campaignId = :campaignId
        AND characterId = :characterId
        ORDER BY joinedAt DESC
    """)
    fun getCharacterCampaignHistory(
        campaignId: String,
        characterId: String
    ): Flow<List<CampaignCharacterEntity>>

    // Retrieves every campaign assignment associated with a character,
    // including assignments from different campaigns.
    // The most recent participation appears first.
    @Query("""
        SELECT * FROM campaign_characters
        WHERE characterId = :characterId
        ORDER BY joinedAt DESC
    """)
    fun getCharacterParticipationHistory(
        characterId: String
    ): Flow<List<CampaignCharacterEntity>>

    // Finds the most recent previous assignment of a character to a campaign
    // by a specific user. This includes inactive assignments.
    // LIMIT 1 ensures that only the latest matching assignment is returned.
    @Query("""
        SELECT * FROM campaign_characters
        WHERE campaignId = :campaignId
        AND userId = :userId
        AND characterId = :characterId
        ORDER BY joinedAt DESC
        LIMIT 1
    """)
    suspend fun getPreviousCharacterAssignment(
        campaignId: String,
        userId: String,
        characterId: String
    ): CampaignCharacterEntity?

    // Marks a character assignment as inactive when the user leaves a campaign.
    // The leftAt field records when the assignment ended.
    @Query("""
        UPDATE campaign_characters
        SET isActive = 0, leftAt = :leftAt
        WHERE id = :assignmentId
    """)
    suspend fun leaveCampaign(
        assignmentId: String,
        leftAt: Long
    )

    // Reactivates a previous character assignment.
    // leftAt is set back to NULL because the character is no longer considered
    // to have a recorded departure time for this active assignment.
    @Query("""
        UPDATE campaign_characters
        SET isActive = 1, leftAt = NULL
        WHERE id = :assignmentId
    """)
    suspend fun reactivateAssignment(
        assignmentId: String
    )

    // Checks whether a user currently has an active character in a campaign.
    // EXISTS returns true if at least one matching record is found, otherwise false.
    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM campaign_characters
            WHERE campaignId = :campaignId
            AND userId = :userId
            AND isActive = 1
        )
    """)
    suspend fun hasActiveCharacter(
        campaignId: String,
        userId: String
    ): Boolean

    // Counts the number of active character assignments in a campaign.
    // Inactive assignments are excluded from the count.
    @Query("""
        SELECT COUNT(*) FROM campaign_characters
        WHERE campaignId = :campaignId
        AND isActive = 1
    """)
    suspend fun getActiveCharacterCount(
        campaignId: String
    ): Int
}
