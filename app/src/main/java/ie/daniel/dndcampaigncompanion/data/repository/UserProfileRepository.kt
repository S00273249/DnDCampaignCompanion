package ie.daniel.dndcampaigncompanion.data.repository

import ie.daniel.dndcampaigncompanion.data.local.dao.UserProfileDao
import ie.daniel.dndcampaigncompanion.data.local.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * Handles user profile database operations.
 *
 * This repository allows profiles to be created, retrieved, updated and deleted.
 * It also provides methods for observing profile changes and checking whether
 * a profile exists.
 */
class UserProfileRepository(
    private val userProfileDao: UserProfileDao
) {

    // Observes all user profiles stored in the database.
    // The Flow allows the UI to receive updates when profiles change.
    fun getAllProfiles(): Flow<List<UserProfileEntity>> {
        return userProfileDao.getAllProfiles()
    }

    // Observes a specific profile using its ID.
    // Returns null if the profile does not exist.
    // Changes to the profile are automatically emitted through the Flow.
    fun observeProfileById(profileId: String): Flow<UserProfileEntity?> {
        return userProfileDao.observeProfileById(profileId)
    }

    // Retrieves a profile once using its ID.
    // Unlike observeProfileById(), this does not continuously observe changes.
    suspend fun getProfileById(profileId: String): UserProfileEntity? {
        return userProfileDao.getProfileById(profileId)
    }

    // Creates a new user profile with a unique ID.
    suspend fun createProfile(
        username: String,
        profileImageUri: String? = null
    ): UserProfileEntity {

        // Creates the profile using the supplied username and optional profile image.
        // The username is trimmed to remove unnecessary whitespace.
        val profile = UserProfileEntity(
            id = UUID.randomUUID().toString(),
            username = username.trim(),
            profileImageUri = profileImageUri,
            createdAt = System.currentTimeMillis()
        )

        // Saves the new profile in the database.
        userProfileDao.insertProfile(profile)

        // Returns the newly created profile.
        return profile
    }

    // Updates the details of an existing profile.
    suspend fun updateProfile(profile: UserProfileEntity) {

        // Creates an updated copy with the username trimmed.
        // Other profile information is preserved from the supplied object.
        userProfileDao.updateProfile(
            profile.copy(username = profile.username.trim())
        )
    }

    // Updates or clears the profile image.
    suspend fun updateProfileImage(
        profile: UserProfileEntity,
        imageUri: String?
    ) {

        // Creates an updated copy with the new image URI.
        // Passing null removes the profile image reference.
        // Other profile information is preserved from the supplied object.
        userProfileDao.updateProfile(
            profile.copy(profileImageUri = imageUri)
        )
    }

    // Deletes a profile from the database using its ID.
    suspend fun deleteProfile(profileId: String) {

        // Retrieves the profile before attempting to delete it.
        // If no matching profile exists, the function simply returns without doing anything.
        val profile = userProfileDao.getProfileById(profileId) ?: return

        // Permanently deletes the profile record from the database.
        userProfileDao.deleteProfile(profile)
    }

    // Checks whether a profile with the specified ID exists in the database.
    suspend fun profileExists(profileId: String): Boolean {
        return userProfileDao.profileExists(profileId)
    }
}
