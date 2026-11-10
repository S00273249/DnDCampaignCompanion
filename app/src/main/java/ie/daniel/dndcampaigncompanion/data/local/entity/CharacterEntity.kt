package ie.daniel.dndcampaigncompanion.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents a player character saved by a user.
 *
 * This entity stores the character's personal information, appearance,
 * background and ability scores. Each character is linked to the user
 * who created and owns it.
 *
 * Characters can be soft deleted, allowing them to be hidden from normal
 * queries without permanently removing their data from the database.
 */
@Entity(
    tableName = "characters",
    foreignKeys = [
        // Links the character to the user who owns it.
        // RESTRICT prevents a user profile from being deleted while their characters reference it.
        ForeignKey(
            entity = UserProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["ownerId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        // Improves queries that search for characters using their owner's user ID.
        Index("ownerId")
    ]
)
data class CharacterEntity(
    // Unique ID used to identify the character.
    @PrimaryKey
    val id: String,

    // ID of the user who owns the character.
    val ownerId: String,

    // Name of the character.
    val name: String,

    // Optional species or race of the character.
    val species: String? = null,

    // Optional character class, such as Fighter, Wizard or Rogue.
    val characterClass: String? = null,

    // Optional subclass that provides additional specialisation within the class.
    val subclass: String? = null,

    // Optional background describing the character's history before adventuring.
    val background: String? = null,

    // Optional alignment describing the character's moral and ethical outlook.
    val alignment: String? = null,

    // Optional URI pointing to the character's portrait image.
    val portraitUri: String? = null,

    // Optional general description of the character.
    val description: String? = null,

    // Optional backstory describing the character's history and experiences.
    val backstory: String? = null,

    // The character's six ability scores.
    // These are nullable because they may not have been entered yet.
    val strength: Int? = null,

    val dexterity: Int? = null,

    val constitution: Int? = null,

    val intelligence: Int? = null,

    val wisdom: Int? = null,

    val charisma: Int? = null,

    // Records when the character was originally created.
    val createdAt: Long,

    // Records when the character was last updated.
    val updatedAt: Long,

    // Tracks whether the character has been soft deleted.
    // New characters are not deleted by default.
    // Setting this to true hides the character from queries that exclude deleted records,
    // while keeping its information in the database.
    val isDeleted: Boolean = false
)
