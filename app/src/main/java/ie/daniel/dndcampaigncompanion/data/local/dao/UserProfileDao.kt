package ie.daniel.dndcampaigncompanion.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import ie.daniel.dndcampaigncompanion.data.local.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

/**
 * Handles database operations for user profiles.
 *
 * This DAO allows user profiles to be created, updated, deleted and retrieved.
 * It also provides queries for checking whether a profile exists and observing
 * changes to individual profiles or the complete list of profiles.
 */
@Dao
interface UserProfileDao {

    // Inserts a new user profile into the database.
    // ABORT means the insert will fail if it conflicts with an existing record,
    // rather than replacing or ignoring the existing data.
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertProfile(profile: UserProfileEntity)

    // Updates an existing user profile using its primary key.
    @Update
    suspend fun updateProfile(profile: UserProfileEntity)

    // Deletes a user profile from the database using its primary key.
    @Delete
    suspend fun deleteProfile(profile: UserProfileEntity)

    // Retrieves a user profile using its unique ID.
    // Returns null if no matching profile is found.
    @Query("SELECT * FROM user_profiles WHERE id = :profileId")
    suspend fun getProfileById(profileId: String): UserProfileEntity?

    // Retrieves all user profiles, ordered by createdAt in ascending order.
    // This means the oldest profiles appear first.
    // Flow allows the UI to receive updates when profiles are added, changed or deleted.
    @Query("SELECT * FROM user_profiles ORDER BY createdAt ASC")
    fun getAllProfiles(): Flow<List<UserProfileEntity>>

    // Observes a specific user profile using its unique ID.
    // Unlike getProfileById(), this returns a Flow that emits the profile again
    // whenever the matching database record changes.
    // Returns null if the profile does not exist.
    @Query("SELECT * FROM user_profiles WHERE id = :profileId")
    fun observeProfileById(profileId: String): Flow<UserProfileEntity?>

    // Checks whether a user profile exists in the database.
    // EXISTS returns true if a matching profile is found, otherwise false.
    @Query("SELECT EXISTS(SELECT 1 FROM user_profiles WHERE id = :profileId)")
    suspend fun profileExists(profileId: String): Boolean
}
