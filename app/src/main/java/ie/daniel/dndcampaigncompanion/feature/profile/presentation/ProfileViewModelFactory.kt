package ie.daniel.dndcampaigncompanion.feature.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ie.daniel.dndcampaigncompanion.data.local.ActiveProfileDataStore
import ie.daniel.dndcampaigncompanion.data.repository.UserProfileRepository

// Factory responsible for creating ProfileViewModel instances with their required dependencies.
class ProfileViewModelFactory(
    private val userProfileRepository: UserProfileRepository,
    private val activeProfileDataStore: ActiveProfileDataStore
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        // Check whether the requested ViewModel type is ProfileViewModel.
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            // The factory returns a ProfileViewModel as the requested generic ViewModel type.
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(
                userProfileRepository,
                activeProfileDataStore
            ) as T
        }

        // Throw an exception if this factory is asked to create an unsupported ViewModel.
        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}