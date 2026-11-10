package ie.daniel.dndcampaigncompanion.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents a character assigned to a user within a campaign.
 *
 * This entity stores the relationship between a campaign, a character and
 * the user controlling that character. It also stores campaign-specific
 * character information such as level, experience, hit points and ability scores.
 *
 * A character can remain in the database after leaving a campaign.
 * The isActive field tracks whether the assignment is currently active.
 */
@Entity(
    tableName = "campaign_characters",
    foreignKeys = [
        // Links each assignment to an existing campaign.
        // RESTRICT prevents a campaign from being deleted while character assignments reference it.
        ForeignKey(
            entity = CampaignEntity::class,
            parentColumns = ["id"],
            childColumns = ["campaignId"],
            onDelete = ForeignKey.RESTRICT
        ),

        // Links each assignment to an existing character.
        // RESTRICT prevents a character from being deleted while it is assigned to a campaign.
        ForeignKey(
            entity = CharacterEntity::class,
            parentColumns = ["id"],
            childColumns = ["characterId"],
            onDelete = ForeignKey.RESTRICT
        ),

        // Links each assignment to the user who controls the character.
        // RESTRICT prevents a user profile from being deleted while assignments reference it.
        ForeignKey(
            entity = UserProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        // Indexes improve searches and filtering using these individual columns.
        Index("campaignId"),
        Index("characterId"),
        Index("userId"),

        // Composite indexes improve queries that filter using both campaignId and userId,
        // or both campaignId and characterId.
        Index(value = ["campaignId", "userId"]),
        Index(value = ["campaignId", "characterId"])
    ]
)
data class CampaignCharacterEntity(
    // Unique ID for this campaign character assignment.
    // A separate ID allows the same character to have different assignment records over time.
    @PrimaryKey
    val id: String,

    // ID of the campaign this character belongs to.
    val campaignId: String,

    // ID of the character being used in the campaign.
    val characterId: String,

    // ID of the user controlling this character.
    val userId: String,

    // The character's current level within the campaign.
    // New assignments start at level 1 by default.
    val level: Int = 1,

    // The character's current experience points.
    // Starts at 0 by default.
    val experience: Int = 0,

    // Current and maximum hit points for the character.
    // Nullable because these values may not have been set yet.
    val currentHp: Int? = null,

    val maxHp: Int? = null,

    // The character's ability scores within the campaign.
    // These are nullable because they may not have been recorded yet.
    val strength: Int? = null,

    val dexterity: Int? = null,

    val constitution: Int? = null,

    val intelligence: Int? = null,

    val wisdom: Int? = null,

    val charisma: Int? = null,

    // Records when the character joined the campaign.
    val joinedAt: Long,

    // Records when the character left the campaign.
    // Remains null while the assignment is active.
    val leftAt: Long? = null,

    // Tracks whether the character is currently assigned to the campaign.
    // New assignments are active by default.
    // Setting this to false allows the assignment history to be kept in the database.
    val isActive: Boolean = true
)
