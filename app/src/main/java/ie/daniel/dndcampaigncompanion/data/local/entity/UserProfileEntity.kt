package ie.daniel.dndcampaigncompanion.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a user profile stored in the database.
 *
 * This entity stores the user's basic profile information, including
 * their username, optional profile image and account creation date.
 */
@Entity(tableName = "user_profiles")
data class UserProfileEntity(

    // Unique ID used to identify the user profile.
    @PrimaryKey
    val id: String,

    // Username displayed when referring to the user throughout the app.
    val username: String,

    // Optional URI pointing to the user's profile image.
    val profileImageUri: String? = null,

    // Records when the user profile was created.
    val createdAt: Long
)
