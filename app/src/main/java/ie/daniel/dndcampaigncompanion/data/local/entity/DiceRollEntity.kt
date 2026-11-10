package ie.daniel.dndcampaigncompanion.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents a dice roll recorded within a campaign.
 *
 * This entity stores the dice selected, individual results, modifier and final
 * total. Each roll is linked to the campaign and user who made it, and can
 * optionally be associated with a specific session.
 */
@Entity(
    tableName = "dice_rolls",
    foreignKeys = [
        // Links the dice roll to an existing campaign.
        // RESTRICT prevents a campaign from being deleted while dice rolls reference it.
        ForeignKey(
            entity = CampaignEntity::class,
            parentColumns = ["id"],
            childColumns = ["campaignId"],
            onDelete = ForeignKey.RESTRICT
        ),

        // Links the dice roll to the user who made it.
        // RESTRICT prevents a user profile from being deleted while their dice rolls reference it.
        ForeignKey(
            entity = UserProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.RESTRICT
        ),

        // Links the dice roll to a session when it was made during one.
        // RESTRICT prevents a session from being deleted while dice rolls reference it.
        ForeignKey(
            entity = SessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        // Indexes improve searches and filtering using these individual columns.
        Index("campaignId"),
        Index("userId"),
        Index("sessionId"),

        // Composite indexes improve queries that filter by campaign and sort by roll time,
        // or filter by both campaign and user.
        Index(value = ["campaignId", "rolledAt"]),
        Index(value = ["campaignId", "userId"])
    ]
)
data class DiceRollEntity(
    // Unique ID used to identify the dice roll.
    @PrimaryKey
    val id: String,

    // ID of the campaign the roll belongs to.
    val campaignId: String,

    // ID of the user who made the roll.
    val userId: String,

    // Optional ID of the session in which the roll was made.
    // Remains null when the roll is not associated with a specific session.
    val sessionId: String? = null,

    // Stores the dice selected for the roll as a JSON String.
    // JSON allows multiple dice selections and their quantities to be stored together.
    val diceSelectionsJson: String,

    // Stores the individual results of each die as a JSON String.
    // This keeps the results separate so they can be retrieved and displayed individually.
    val individualResultsJson: String,

    // Modifier added to the dice results, such as a character's ability modifier.
    val modifier: Int,

    // Sum of the individual dice results before the modifier is applied.
    val subtotal: Int,

    // Final result after the modifier has been added to the subtotal.
    val total: Int,

    // Optional description explaining the purpose of the roll.
    val description: String? = null,

    // Records when the dice roll was made, stored as a timestamp.
    val rolledAt: Long
)
