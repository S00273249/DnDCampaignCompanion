package ie.daniel.dndcampaigncompanion.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Creates a DataStore instance for storing user preferences.
// The name identifies the preferences file used by the application.
private val Context.dataStore by preferencesDataStore(
    name = "user_preferences"
)

/**
 * Handles storing and retrieving the currently active user profile.
 *
 * DataStore is used to save the active profile ID so that the selected
 * profile can be remembered when the app is closed and reopened.
 */
class ActiveProfileDataStore(
    private val context: Context
) {

    companion object {
        // Key used to store and retrieve the active profile ID in DataStore.
        // stringPreferencesKey means the stored value will be a String.
        private val ACTIVE_PROFILE_ID =
            stringPreferencesKey("active_profile_id")
    }

    // Observes the currently active profile ID from DataStore.
    // map extracts only the value associated with ACTIVE_PROFILE_ID
    // instead of returning all stored preferences.
    // Returns null if no active profile ID has been saved.
    val activeProfileId: Flow<String?> =
        context.dataStore.data.map { preferences ->
            preferences[ACTIVE_PROFILE_ID]
        }

    // Saves the selected profile ID in DataStore.
    // This allows the app to remember which profile was last active.
    suspend fun setActiveProfile(profileId: String) {
        context.dataStore.edit { preferences ->
            preferences[ACTIVE_PROFILE_ID] = profileId
        }
    }

    // Removes the active profile ID from DataStore.
    // After this, activeProfileId will emit null because no profile is saved.
    suspend fun clearActiveProfile() {
        context.dataStore.edit { preferences ->
            preferences.remove(ACTIVE_PROFILE_ID)
        }
    }
}
