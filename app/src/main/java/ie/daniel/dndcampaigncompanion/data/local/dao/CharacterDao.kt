package ie.daniel.dndcampaigncompanion.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import ie.daniel.dndcampaigncompanion.data.local.entity.CharacterEntity
import kotlinx.coroutines.flow.Flow

/**
 * Handles database operations for player characters.
 *
 * This DAO allows characters to be created, updated, retrieved, soft deleted
 * and restored. It also provides queries for retrieving characters belonging
 * to a specific user and checking whether a character exists.
 */
@Dao
interface CharacterDao {

    // Inserts a new character into the database.
    // ABORT means the insert will fail if it conflicts with an existing record,
    // rather than replacing or ignoring the existing data.
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertCharacter(character: CharacterEntity)

    // Updates an existing character using its primary key.
    @Update
    suspend fun updateCharacter(character: CharacterEntity)

    // Retrieves a character using its unique ID.
    // Returns null if no matching character is found.
    @Query("SELECT * FROM characters WHERE id = :characterId")
    suspend fun getCharacterById(characterId: String): CharacterEntity?

    // Retrieves all characters owned by a specific user that have not been deleted.
    // Results are ordered by updatedAt in descending order,
    // so the most recently updated characters appear first.
    // Flow allows the UI to receive updates when the matching character records change.
    @Query("""
        SELECT * FROM characters
        WHERE ownerId = :ownerId
        AND isDeleted = 0
        ORDER BY updatedAt DESC
    """)
    fun getUserCharacters(ownerId: String): Flow<List<CharacterEntity>>

    // Retrieves a specific character belonging to a user.
    // Both the character ID and owner ID must match.
    // Deleted characters are excluded from the result.
    @Query("""
        SELECT * FROM characters
        WHERE ownerId = :ownerId
        AND id = :characterId
        AND isDeleted = 0
    """)
    suspend fun getUserCharacter(
        ownerId: String,
        characterId: String
    ): CharacterEntity?

    // Retrieves all characters owned by a user that have not been deleted.
    // Results are ordered alphabetically by character name.
    // Unlike getUserCharacters(), this returns a regular List rather than a Flow,
    // so it provides a one-time result instead of continuously observing changes.
    @Query("""
        SELECT * FROM characters
        WHERE ownerId = :ownerId
        AND isDeleted = 0
        ORDER BY name ASC
    """)
    suspend fun getAvailableCharacters(
        ownerId: String
    ): List<CharacterEntity>

    // Soft deletes a character by setting isDeleted to 1 instead of removing
    // the record from the database.
    // The updatedAt field records when the character was deleted.
    // The owner ID check ensures that only the character's owner can be targeted.
    @Query("""
        UPDATE characters
        SET isDeleted = 1, updatedAt = :updatedAt
        WHERE id = :characterId
        AND ownerId = :ownerId
    """)
    suspend fun softDeleteCharacter(
        characterId: String,
        ownerId: String,
        updatedAt: Long
    )

    // Restores a previously deleted character by setting isDeleted back to 0.
    // The updatedAt field records when the character was restored.
    // The owner ID check ensures that only the character's owner can be targeted.
    @Query("""
        UPDATE characters
        SET isDeleted = 0, updatedAt = :updatedAt
        WHERE id = :characterId
        AND ownerId = :ownerId
    """)
    suspend fun restoreCharacter(
        characterId: String,
        ownerId: String,
        updatedAt: Long
    )

    // Checks whether a character exists, belongs to the specified user
    // and has not been deleted.
    // EXISTS returns true if a matching record is found, otherwise false.
    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM characters
            WHERE id = :characterId
            AND ownerId = :ownerId
            AND isDeleted = 0
        )
    """)
    suspend fun characterExists(
        characterId: String,
        ownerId: String
    ): Boolean
}
