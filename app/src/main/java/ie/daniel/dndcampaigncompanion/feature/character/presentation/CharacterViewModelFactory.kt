package ie.daniel.dndcampaigncompanion.feature.character.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ie.daniel.dndcampaigncompanion.data.local.ActiveProfileDataStore
import ie.daniel.dndcampaigncompanion.data.repository.CharacterRepository

/**
 * Creates CharacterViewModel instances with their required dependencies.
 *
 * Used because CharacterViewModel requires a repository and DataStore
 * rather than having a default no-argument constructor.
 */
class CharacterViewModelFactory(
    private val characterRepository: CharacterRepository,
    private val activeProfileDataStore: ActiveProfileDataStore
) : ViewModelProvider.Factory {

    // Called by ViewModelProvider when a ViewModel instance is requested.
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        // Check whether the requested ViewModel is CharacterViewModel.
        if (modelClass.isAssignableFrom(CharacterViewModel::class.java)) {
            // Supply the required dependencies and return the new ViewModel.
            // The cast is unchecked because the generic type cannot be verified
            // at runtime, but the class check above ensures the correct type.
            return CharacterViewModel(
                characterRepository = characterRepository,
                activeProfileDataStore = activeProfileDataStore
            ) as T
        }

        // Prevent the factory from creating an unsupported ViewModel type.
        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}