package ie.daniel.dndcampaigncompanion.feature.character.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ie.daniel.dndcampaigncompanion.data.local.ActiveProfileDataStore
import ie.daniel.dndcampaigncompanion.data.local.entity.CharacterEntity
import ie.daniel.dndcampaigncompanion.data.repository.CharacterRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Represents the state of the character list, including the active profile,
// the characters belonging to it, and any loading or error information.
data class CharacterListUiState(
    val characters: List<CharacterEntity> = emptyList(),
    val activeProfileId: String? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

// Tracks whether a character operation is running and stores any error
// returned when creating, updating, deleting or restoring a character.
data class CharacterOperationUiState(
    val isProcessing: Boolean = false,
    val errorMessage: String? = null
)

// Holds the details of an individual character and the state of its loading operation.
data class CharacterDetailUiState(
    val character: CharacterEntity? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class CharacterViewModel(
    private val characterRepository: CharacterRepository,
    private val activeProfileDataStore: ActiveProfileDataStore
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    // Observe the active profile and automatically switch the character list
    // whenever a different profile is selected.
    val listUiState: StateFlow<CharacterListUiState> =
        activeProfileDataStore.activeProfileId
            .flatMapLatest { profileId ->
                // If there is no active profile, return an empty list state
                // instead of attempting to query the repository.
                if (profileId.isNullOrBlank()) {
                    flowOf(
                        CharacterListUiState(
                            activeProfileId = null,
                            isLoading = false
                        )
                    )
                } else {
                    // Observe the characters belonging to the selected profile.
                    // flatMapLatest above ensures the previous profile's observation
                    // is cancelled when another profile becomes active.
                    characterRepository.getUserCharacters(profileId)
                        .combine(flowOf(profileId)) { characters, activeProfileId ->
                            CharacterListUiState(
                                characters = characters,
                                activeProfileId = activeProfileId,
                                isLoading = false
                            )
                        }
                }
            }
            // Convert the Flow into a StateFlow that can be collected by Compose.
            // The upstream flow remains active for five seconds after the last
            // subscriber disappears, helping avoid unnecessary restarts.
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = CharacterListUiState()
            )

    // Mutable state is kept private so screens cannot directly change character details.
    private val _detailUiState =
        MutableStateFlow(CharacterDetailUiState())

    // Expose the detail state as a read-only StateFlow for the UI.
    val detailUiState: StateFlow<CharacterDetailUiState> =
        _detailUiState.asStateFlow()

    // Private mutable state for character operations.
    private val _operationUiState =
        MutableStateFlow(CharacterOperationUiState())

    // Screens can observe operation progress and errors without modifying the state.
    val operationUiState: StateFlow<CharacterOperationUiState> =
        _operationUiState.asStateFlow()

    fun loadCharacter(characterId: String) {
        viewModelScope.launch {
            // Reset the detail state and show loading while retrieving the character.
            _detailUiState.value = CharacterDetailUiState(
                isLoading = true
            )

            try {
                // Retrieve the currently selected profile so the character lookup
                // can be restricted to the profile that owns the character.
                val ownerId = activeProfileDataStore.activeProfileId.first()

                require(!ownerId.isNullOrBlank()) {
                    "No active profile is selected."
                }

                val character = characterRepository.getUserCharacter(
                    ownerId = ownerId,
                    characterId = characterId
                )

                // Update the UI state depending on whether the character was found.
                _detailUiState.value = if (character != null) {
                    CharacterDetailUiState(
                        character = character,
                        isLoading = false
                    )
                } else {
                    CharacterDetailUiState(
                        isLoading = false,
                        errorMessage = "Character not found."
                    )
                }

            } catch (exception: CancellationException) {
                // Allow coroutine cancellation to propagate instead of treating
                // it as an ordinary loading error.
                throw exception
            } catch (exception: Exception) {
                // Report unexpected errors through the detail UI state.
                _detailUiState.value = CharacterDetailUiState(
                    isLoading = false,
                    errorMessage = exception.message
                        ?: "Unable to load character."
                )
            }
        }
    }

    fun createCharacter(
        name: String,
        species: String?,
        characterClass: String?,
        subclass: String?,
        background: String?,
        alignment: String?,
        description: String?,
        backstory: String?,
        strength: Int?,
        dexterity: Int?,
        constitution: Int?,
        intelligence: Int?,
        wisdom: Int?,
        charisma: Int?,
        onSuccess: (CharacterEntity) -> Unit
    ) {
        viewModelScope.launch {
            // Indicate that the character is currently being created.
            _operationUiState.value = CharacterOperationUiState(
                isProcessing = true
            )

            try {
                // Get the active profile so the new character is assigned
                // to the correct owner.
                val ownerId = activeProfileDataStore.activeProfileId.first()

                require(!ownerId.isNullOrBlank()) {
                    "No active profile is selected."
                }

                // Pass the character information to the repository for storage.
                val character = characterRepository.createCharacter(
                    ownerId = ownerId,
                    name = name,
                    species = species,
                    characterClass = characterClass,
                    subclass = subclass,
                    background = background,
                    alignment = alignment,
                    description = description,
                    backstory = backstory,
                    strength = strength,
                    dexterity = dexterity,
                    constitution = constitution,
                    intelligence = intelligence,
                    wisdom = wisdom,
                    charisma = charisma
                )

                // Reset the operation state after a successful save.
                _operationUiState.value = CharacterOperationUiState()

                // Return the newly created character to the calling screen.
                onSuccess(character)

            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                // Store an error message and stop the processing state if creation fails.
                _operationUiState.value = CharacterOperationUiState(
                    errorMessage = exception.message
                        ?: "Unable to create character. Please try again."
                )
            }
        }
    }

    fun updateCharacter(
        character: CharacterEntity,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _operationUiState.value = CharacterOperationUiState(
                isProcessing = true
            )

            try {
                // Retrieve the active profile and verify that the character
                // belongs to it before allowing the update.
                val ownerId = activeProfileDataStore.activeProfileId.first()

                require(!ownerId.isNullOrBlank()) {
                    "No active profile is selected."
                }

                require(character.ownerId == ownerId) {
                    "You can only edit your own characters."
                }

                // Save the updated character through the repository.
                characterRepository.updateCharacter(character)

                // Update the detail state with the saved character.
                // The timestamp is refreshed so the UI reflects the latest update.
                _detailUiState.value = CharacterDetailUiState(
                    character = character.copy(
                        updatedAt = System.currentTimeMillis()
                    ),
                    isLoading = false
                )

                _operationUiState.value = CharacterOperationUiState()

                // Notify the calling screen that the update succeeded.
                onSuccess()

            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _operationUiState.value = CharacterOperationUiState(
                    errorMessage = exception.message
                        ?: "Unable to update character. Please try again."
                )
            }
        }
    }

    fun deleteCharacter(
        characterId: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _operationUiState.value = CharacterOperationUiState(
                isProcessing = true
            )

            try {
                // Retrieve the active profile to ensure the deletion
                // is performed against the correct character owner.
                val ownerId = activeProfileDataStore.activeProfileId.first()

                require(!ownerId.isNullOrBlank()) {
                    "No active profile is selected."
                }

                // Request deletion through the repository.
                // The repository handles the ownership check and soft deletion.
                characterRepository.deleteCharacter(
                    ownerId = ownerId,
                    characterId = characterId
                )

                _operationUiState.value = CharacterOperationUiState()

                // Notify the calling screen so it can navigate away or refresh.
                onSuccess()

            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _operationUiState.value = CharacterOperationUiState(
                    errorMessage = exception.message
                        ?: "Unable to delete character. Please try again."
                )
            }
        }
    }

    fun restoreCharacter(characterId: String) {
        viewModelScope.launch {
            _operationUiState.value = CharacterOperationUiState(
                isProcessing = true
            )

            try {
                // Retrieve the active profile so only its own character
                // can be restored through the repository.
                val ownerId = activeProfileDataStore.activeProfileId.first()

                require(!ownerId.isNullOrBlank()) {
                    "No active profile is selected."
                }

                characterRepository.restoreCharacter(
                    ownerId = ownerId,
                    characterId = characterId
                )

                // Clear the processing state after a successful restoration.
                _operationUiState.value = CharacterOperationUiState()

            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _operationUiState.value = CharacterOperationUiState(
                    errorMessage = exception.message
                        ?: "Unable to restore character. Please try again."
                )
            }
        }
    }
}