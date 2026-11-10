package ie.daniel.dndcampaigncompanion.data.repository

import ie.daniel.dndcampaigncompanion.data.local.dao.CharacterDao
import ie.daniel.dndcampaigncompanion.data.local.entity.CharacterEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * Handles character-related database operations.
 *
 * This repository allows users to create and manage their own saved characters.
 * It also provides methods for retrieving available characters, updating details,
 * and deleting or restoring character records.
 */
class CharacterRepository(
    private val characterDao: CharacterDao
) {

    // Observes all non-deleted characters belonging to a specific user.
    // The Flow allows the UI to receive updates when the user's characters change.
    fun getUserCharacters(ownerId: String): Flow<List<CharacterEntity>> {
        return characterDao.getUserCharacters(ownerId)
    }

    // Retrieves a character using its unique ID.
    // Returns null if no matching character exists.
    suspend fun getCharacterById(characterId: String): CharacterEntity? {
        return characterDao.getCharacterById(characterId)
    }

    // Retrieves a character only if it belongs to the specified user.
    // This helps prevent users from accessing another user's character through this method.
    suspend fun getUserCharacter(
        ownerId: String,
        characterId: String
    ): CharacterEntity? {
        return characterDao.getUserCharacter(ownerId, characterId)
    }

    // Retrieves characters belonging to a user that are available for assignment.
    // This excludes characters that are already assigned or have been deleted,
    // depending on the conditions defined in the DAO query.
    suspend fun getAvailableCharacters(
        ownerId: String
    ): List<CharacterEntity> {
        return characterDao.getAvailableCharacters(ownerId)
    }

    // Creates a new character and saves it to the database.
    suspend fun createCharacter(
        ownerId: String,
        name: String,
        species: String? = null,
        characterClass: String? = null,
        subclass: String? = null,
        background: String? = null,
        alignment: String? = null,
        portraitUri: String? = null,
        description: String? = null,
        backstory: String? = null,
        strength: Int? = null,
        dexterity: Int? = null,
        constitution: Int? = null,
        intelligence: Int? = null,
        wisdom: Int? = null,
        charisma: Int? = null
    ): CharacterEntity {

        // Makes sure the character has a name before it is saved.
        require(name.isNotBlank()) {
            "Character name cannot be empty."
        }

        // Uses the same timestamp for the character's creation and last update times.
        val currentTime = System.currentTimeMillis()

        // Creates a new character with a unique ID and the supplied details.
        // Optional fields can remain null if the user has not provided them.
        val character = CharacterEntity(
            id = UUID.randomUUID().toString(),
            ownerId = ownerId,
            name = name.trim(),
            species = species?.trim()?.takeIf { it.isNotEmpty() },
            characterClass = characterClass?.trim()?.takeIf { it.isNotEmpty() },
            subclass = subclass?.trim()?.takeIf { it.isNotEmpty() },
            background = background?.trim()?.takeIf { it.isNotEmpty() },
            alignment = alignment?.trim()?.takeIf { it.isNotEmpty() },
            portraitUri = portraitUri,
            description = description?.trim()?.takeIf { it.isNotEmpty() },
            backstory = backstory?.trim()?.takeIf { it.isNotEmpty() },
            strength = strength,
            dexterity = dexterity,
            constitution = constitution,
            intelligence = intelligence,
            wisdom = wisdom,
            charisma = charisma,
            createdAt = currentTime,
            updatedAt = currentTime
        )

        // Saves the new character in the database.
        characterDao.insertCharacter(character)

        // Returns the newly created character.
        return character
    }

    // Updates the details of an existing character.
    suspend fun updateCharacter(character: CharacterEntity) {

        // Retrieves the existing character using both its ID and owner ID.
        // This ensures that the character belongs to the user attempting to update it.
        val existingCharacter = characterDao.getUserCharacter(
            character.ownerId,
            character.id
        ) ?: throw IllegalArgumentException(
            "Character not found or does not belong to this user."
        )

        // Makes sure the updated character still has a valid name.
        require(character.name.isNotBlank()) {
            "Character name cannot be empty."
        }

        // Saves an updated copy of the character.
        // The creation timestamp is preserved so updating a character does not
        // change when it was originally created.
        // The updated timestamp records when these changes were made.
        characterDao.updateCharacter(
            character.copy(
                name = character.name.trim(),
                updatedAt = System.currentTimeMillis(),
                createdAt = existingCharacter.createdAt
            )
        )
    }

    // Soft-deletes a character belonging to the specified user.
    // The character record remains in the database but is marked as deleted.
    suspend fun deleteCharacter(
        ownerId: String,
        characterId: String
    ) {

        // Checks that the character exists and belongs to the user before deleting it.
        val character = characterDao.getUserCharacter(
            ownerId,
            characterId
        ) ?: throw IllegalArgumentException(
            "Character not found or does not belong to this user."
        )

        // Marks the character as deleted and records when it was deleted.
        // This allows the character to be restored later.
        characterDao.softDeleteCharacter(
            characterId,
            ownerId,
            System.currentTimeMillis()
        )
    }

    // Restores a previously soft-deleted character belonging to the specified user.
    suspend fun restoreCharacter(
        ownerId: String,
        characterId: String
    ) {
        // Clears the deleted status and updates the character's last update time.
        characterDao.restoreCharacter(
            characterId,
            ownerId,
            System.currentTimeMillis()
        )
    }

    // Checks whether a character exists for a specific user.
    // The DAO query determines whether deleted characters are included.
    suspend fun characterExists(
        characterId: String,
        ownerId: String
    ): Boolean {
        return characterDao.characterExists(characterId, ownerId)
    }
}
