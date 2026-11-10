package ie.daniel.dndcampaigncompanion.feature.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ie.daniel.dndcampaigncompanion.data.local.ActiveProfileDataStore
import ie.daniel.dndcampaigncompanion.data.local.entity.UserProfileEntity
import ie.daniel.dndcampaigncompanion.data.repository.UserProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Holds the profile list and identifies which profile is currently active.
data class ProfileUiState(
    val profiles: List<UserProfileEntity> = emptyList(),
    val activeProfileId: String? = null,
    val isLoading: Boolean = true
)

// Tracks the progress and any error returned during profile creation.
data class ProfileCreationUiState(
    val isCreating: Boolean = false,
    val errorMessage: String? = null
)

class ProfileViewModel(
    private val userProfileRepository: UserProfileRepository,
    private val activeProfileDataStore: ActiveProfileDataStore
) : ViewModel() {

    // Combine the saved profiles with the currently active profile ID.
    // Whenever either Flow emits a new value, the UI state is updated.
    val uiState: StateFlow<ProfileUiState> =
        combine(
            userProfileRepository.getAllProfiles(),
            activeProfileDataStore.activeProfileId
        ) { profiles, activeProfileId ->
            ProfileUiState(
                profiles = profiles,
                activeProfileId = activeProfileId,
                isLoading = false
            )
        }.stateIn(
            // Keep the combined Flow within the ViewModel lifecycle.
            scope = viewModelScope,
            // Keep it active while the UI is subscribed, with a 5-second
            // delay before stopping collection when there are no subscribers.
            started = SharingStarted.WhileSubscribed(5_000),
            // Use the default loading state until the first values are received.
            initialValue = ProfileUiState()
        )

    // Private mutable state used to track profile creation.
    private val _creationUiState =
        MutableStateFlow(ProfileCreationUiState())

    // Expose a read-only version so the UI can observe but not directly change this state.
    val creationUiState: StateFlow<ProfileCreationUiState> =
        _creationUiState.asStateFlow()

    // Save the selected profile ID as the active profile on the device.
    fun selectProfile(profileId: String) {
        viewModelScope.launch {
            activeProfileDataStore.setActiveProfile(profileId)
        }
    }

    // Remove the currently stored active profile ID from DataStore.
    fun clearActiveProfile() {
        viewModelScope.launch {
            activeProfileDataStore.clearActiveProfile()
        }
    }

    fun createProfile(
        username: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            // Indicate that profile creation has started and clear any previous error.
            _creationUiState.value = ProfileCreationUiState(
                isCreating = true
            )

            try {
                // Create the profile through the repository, trimming any surrounding whitespace.
                val profile = userProfileRepository.createProfile(
                    username = username.trim()
                )

                // Automatically make the newly created profile the active profile.
                activeProfileDataStore.setActiveProfile(profile.id)

                // Reset the creation state after both operations succeed.
                _creationUiState.value = ProfileCreationUiState()

                // Notify the screen that the profile is ready.
                onSuccess()

            } catch (exception: Exception) {
                // Display a general error message if profile creation or activation fails.
                _creationUiState.value = ProfileCreationUiState(
                    errorMessage = "Unable to create profile. Please try again."
                )
            }
        }
    }

    fun updateUsername(
        profileId: String,
        username: String
    ) {
        viewModelScope.launch {
            // Retrieve the existing profile so its other fields are preserved.
            val existingProfile =
                userProfileRepository.getProfileById(profileId)
                    ?: return@launch

            // Create an updated copy with the new username and save it through the repository.
            userProfileRepository.updateProfile(
                existingProfile.copy(
                    username = username.trim()
                )
            )
        }
    }

    fun updateProfileImage(
        profileId: String,
        imageUri: String?
    ) {
        viewModelScope.launch {
            // Retrieve the existing profile before changing its image.
            val existingProfile =
                userProfileRepository.getProfileById(profileId)
                    ?: return@launch

            // Pass the existing profile and new URI to the repository.
            // A null URI can be used to clear the current profile image.
            userProfileRepository.updateProfileImage(
                existingProfile,
                imageUri
            )
        }
    }
}