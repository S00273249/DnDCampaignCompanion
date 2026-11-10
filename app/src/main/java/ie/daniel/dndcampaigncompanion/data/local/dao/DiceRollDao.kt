package ie.daniel.dndcampaigncompanion.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ie.daniel.dndcampaigncompanion.data.local.entity.DiceRollEntity
import kotlinx.coroutines.flow.Flow

/**
 * Handles database operations for dice rolls.
 *
 * This DAO allows dice rolls to be saved and retrieved.
 * It provides queries for viewing rolls across an entire campaign,
 * by a specific user or within an individual session.
 */
@Dao
interface DiceRollDao {

    // Inserts a new dice roll record into the database.
    // ABORT means the insert will fail if it conflicts with an existing record,
    // rather than replacing or ignoring the existing data.
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertDiceRoll(diceRoll: DiceRollEntity)

    // Retrieves a dice roll using its unique ID.
    // Returns null if no matching roll is found.
    @Query("SELECT * FROM dice_rolls WHERE id = :rollId")
    suspend fun getDiceRollById(rollId: String): DiceRollEntity?

    // Retrieves all dice rolls belonging to a specific campaign.
    // Results are ordered by rolledAt in descending order,
    // so the most recent rolls appear first.
    // Flow allows the UI to receive updates when new rolls are added.
    @Query("""
        SELECT * FROM dice_rolls
        WHERE campaignId = :campaignId
        ORDER BY rolledAt DESC
    """)
    fun getCampaignDiceRolls(
        campaignId: String
    ): Flow<List<DiceRollEntity>>

    // Retrieves dice rolls made by a specific user within a campaign.
    // Both the campaign ID and user ID must match.
    // The most recent rolls appear first.
    @Query("""
        SELECT * FROM dice_rolls
        WHERE campaignId = :campaignId
        AND userId = :userId
        ORDER BY rolledAt DESC
    """)
    fun getUserDiceRolls(
        campaignId: String,
        userId: String
    ): Flow<List<DiceRollEntity>>

    // Retrieves dice rolls belonging to a specific session within a campaign.
    // Both the campaign ID and session ID must match.
    // The most recent rolls appear first.
    @Query("""
        SELECT * FROM dice_rolls
        WHERE campaignId = :campaignId
        AND sessionId = :sessionId
        ORDER BY rolledAt DESC
    """)
    fun getSessionDiceRolls(
        campaignId: String,
        sessionId: String
    ): Flow<List<DiceRollEntity>>

    // Retrieves a limited number of the most recent dice rolls in a campaign.
    // LIMIT controls how many records are returned.
    // For example, a limit of 10 returns the 10 most recent rolls.
    // Unlike the Flow queries above, this returns a one-time List.
    @Query("""
        SELECT * FROM dice_rolls
        WHERE campaignId = :campaignId
        ORDER BY rolledAt DESC
        LIMIT :limit
    """)
    suspend fun getRecentDiceRolls(
        campaignId: String,
        limit: Int
    ): List<DiceRollEntity>

    // Counts the total number of dice rolls recorded for a campaign.
    @Query("""
        SELECT COUNT(*) FROM dice_rolls
        WHERE campaignId = :campaignId
    """)
    suspend fun getCampaignDiceRollCount(
        campaignId: String
    ): Int
}
