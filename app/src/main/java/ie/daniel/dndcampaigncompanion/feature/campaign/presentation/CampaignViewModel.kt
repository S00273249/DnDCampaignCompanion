package ie.daniel.dndcampaigncompanion.feature.campaign.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ie.daniel.dndcampaigncompanion.data.local.ActiveProfileDataStore
import ie.daniel.dndcampaigncompanion.data.local.entity.CampaignCharacterEntity
import ie.daniel.dndcampaigncompanion.data.local.entity.CampaignEntity
import ie.daniel.dndcampaigncompanion.data.local.entity.CampaignMembershipEntity
import ie.daniel.dndcampaigncompanion.data.local.entity.CharacterEntity
import ie.daniel.dndcampaigncompanion.data.local.entity.UserProfileEntity
import ie.daniel.dndcampaigncompanion.data.repository.CampaignCharacterRepository
import ie.daniel.dndcampaigncompanion.data.repository.CampaignMembershipRepository
import ie.daniel.dndcampaigncompanion.data.repository.CampaignRepository
import ie.daniel.dndcampaigncompanion.data.repository.CharacterRepository
import ie.daniel.dndcampaigncompanion.data.repository.UserProfileRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Represents a campaign together with the active membership
// that connects the current profile to that campaign.
data class CampaignListItem(
    val campaign: CampaignEntity,
    val membership: CampaignMembershipEntity
)

// Holds the information displayed on the main campaigns screen.
data class CampaignUiState(
    val campaigns: List<CampaignListItem> = emptyList(),
    val activeProfileId: String? = null,
    val isLoading: Boolean = true
)

// Tracks the progress and any errors when creating a campaign.
data class CampaignCreationUiState(
    val isCreating: Boolean = false,
    val errorMessage: String? = null
)

// Tracks the progress and any errors when updating a campaign.
data class CampaignUpdateUiState(
    val isUpdating: Boolean = false,
    val errorMessage: String? = null
)

// Combines a campaign membership with its associated profile
// and assigned character for display on the campaign details screen.
data class CampaignMemberUiItem(
    val membership: CampaignMembershipEntity,
    val profile: UserProfileEntity?,
    val character: CharacterEntity? = null
)

// Holds the campaign details and information about its members.
data class CampaignDetailsUiState(
    val campaign: CampaignEntity? = null,
    val members: List<CampaignMemberUiItem> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

// Holds the profiles that are available to be added to a campaign.
data class AvailableCampaignProfilesUiState(
    val profiles: List<UserProfileEntity> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

// Tracks the progress and any errors when adding a player to a campaign.
data class AddCampaignPlayerUiState(
    val isAdding: Boolean = false,
    val errorMessage: String? = null
)

// Holds the character assignments for a campaign, including
// the assignment belonging to the currently active profile.
data class CampaignCharacterAssignmentsUiState(
    val assignments: List<CampaignCharacterEntity> = emptyList(),
    val myAssignment: CampaignCharacterEntity? = null,
    val activeProfileId: String? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

// Holds the saved characters available to the currently active profile.
data class AvailableCharactersUiState(
    val characters: List<CharacterEntity> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

// Tracks the progress and any errors when assigning or removing
// a character from a campaign.
data class CharacterAssignmentOperationUiState(
    val isProcessing: Boolean = false,
    val errorMessage: String? = null
)

/**
 * Manages campaign-related data and operations for the presentation layer.
 *
 * Connects the campaign repositories and active profile DataStore to the UI.
 * Exposes observable states for campaign information, members and character
 * assignments, and handles operations requested by the screens.
 */
class CampaignViewModel(
    private val campaignRepository: CampaignRepository,
    private val campaignMembershipRepository: CampaignMembershipRepository,
    private val activeProfileDataStore: ActiveProfileDataStore,
    private val userProfileRepository: UserProfileRepository,
    private val campaignCharacterRepository: CampaignCharacterRepository,
    private val characterRepository: CharacterRepository
) : ViewModel() {

    // Observes the active profile and retrieves the campaigns associated with it.
    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<CampaignUiState> =
        activeProfileDataStore.activeProfileId
            .flatMapLatest { profileId ->
                // If no profile is selected, return an empty campaign list.
                if (profileId == null) {
                    flowOf(
                        CampaignUiState(
                            activeProfileId = null,
                            isLoading = false
                        )
                    )
                } else {
                    // Combines the campaign list and active memberships.
                    // This ensures each displayed campaign has a corresponding
                    // active membership for the selected profile.
                    combine(
                        campaignRepository.getCampaignsForUser(profileId),
                        campaignMembershipRepository.getUserActiveMemberships(profileId)
                    ) { campaigns, memberships ->
                        // Create a lookup using campaign IDs so each campaign
                        // can be matched with its corresponding membership.
                        val membershipsByCampaign =
                            memberships.associateBy { it.campaignId }

                        val campaignItems = campaigns.mapNotNull { campaign ->
                            membershipsByCampaign[campaign.id]?.let { membership ->
                                CampaignListItem(
                                    campaign = campaign,
                                    membership = membership
                                )
                            }
                        }

                        CampaignUiState(
                            campaigns = campaignItems,
                            activeProfileId = profileId,
                            isLoading = false
                        )
                    }
                }
            }
            // Keeps the latest state available to the UI.
            // WhileSubscribed stops collecting after a short period without observers.
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = CampaignUiState()
            )

    // Observes the active profile and retrieves its saved characters.
    // flatMapLatest switches to the new profile's character list when
    // the active profile changes.
    @OptIn(ExperimentalCoroutinesApi::class)
    val availableCharactersUiState: StateFlow<AvailableCharactersUiState> =
        activeProfileDataStore.activeProfileId
            .flatMapLatest { profileId ->
                // Return an empty state when there is no active profile.
                if (profileId.isNullOrBlank()) {
                    flowOf(
                        AvailableCharactersUiState(
                            isLoading = false
                        )
                    )
                } else {
                    // Convert the character repository results into UI state.
                    characterRepository.getUserCharacters(profileId)
                        .map { characters ->
                            AvailableCharactersUiState(
                                characters = characters,
                                isLoading = false
                            )
                        }
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = AvailableCharactersUiState()
            )

    // Private state holders allow this ViewModel to update the values
    // while exposing read-only StateFlows to the UI.
    private val _creationUiState =
        MutableStateFlow(CampaignCreationUiState())

    val creationUiState: StateFlow<CampaignCreationUiState> =
        _creationUiState.asStateFlow()

    private val _updateUiState =
        MutableStateFlow(CampaignUpdateUiState())

    val updateUiState: StateFlow<CampaignUpdateUiState> =
        _updateUiState.asStateFlow()

    private val _detailsUiState =
        MutableStateFlow(CampaignDetailsUiState())

    val detailsUiState: StateFlow<CampaignDetailsUiState> =
        _detailsUiState.asStateFlow()

    private val _availableProfilesUiState =
        MutableStateFlow(AvailableCampaignProfilesUiState())

    val availableProfilesUiState: StateFlow<AvailableCampaignProfilesUiState> =
        _availableProfilesUiState.asStateFlow()

    private val _addPlayerUiState =
        MutableStateFlow(AddCampaignPlayerUiState())

    val addPlayerUiState: StateFlow<AddCampaignPlayerUiState> =
        _addPlayerUiState.asStateFlow()

    private val _characterAssignmentsUiState =
        MutableStateFlow(CampaignCharacterAssignmentsUiState())

    val characterAssignmentsUiState: StateFlow<CampaignCharacterAssignmentsUiState> =
        _characterAssignmentsUiState.asStateFlow()

    private val _characterAssignmentOperationUiState =
        MutableStateFlow(CharacterAssignmentOperationUiState())

    val characterAssignmentOperationUiState: StateFlow<CharacterAssignmentOperationUiState> =
        _characterAssignmentOperationUiState.asStateFlow()

    // Keep references to collection jobs so they can be cancelled
    // before starting another collection for a different campaign.
    private var availableProfilesJob: Job? = null
    private var characterAssignmentsJob: Job? = null

    /**
     * Loads a campaign and combines its memberships, profiles and
     * character assignments into one state for the details screen.
     */
    fun loadCampaignDetails(campaignId: String) {
        viewModelScope.launch {
            // Reset the details state so previous campaign information
            // is not displayed while the new campaign is loading.
            _detailsUiState.value = CampaignDetailsUiState()

            try {
                // Combine several database flows so changes to campaign details,
                // memberships, profiles or character assignments update the screen.
                combine(
                    campaignRepository.observeCampaignById(campaignId),
                    campaignMembershipRepository.getActiveCampaignMembers(campaignId),
                    userProfileRepository.getAllProfiles(),
                    campaignCharacterRepository.getActiveCampaignCharacters(campaignId)
                ) { campaign, memberships, profiles, assignments ->

                    // Build lookup maps to match profiles and character assignments
                    // with their corresponding campaign members using user IDs.
                    val profilesById = profiles.associateBy { it.id }
                    val assignmentsByUserId =
                        assignments.associateBy { it.userId }

                    val members = memberships.map { membership ->
                        val assignment =
                            assignmentsByUserId[membership.userId]

                        // Retrieve the assigned character when the member
                        // has an active character assignment.
                        val character = assignment?.let {
                            characterRepository.getCharacterById(it.characterId)
                        }

                        CampaignMemberUiItem(
                            membership = membership,
                            profile = profilesById[membership.userId],
                            character = character
                        )
                    }

                    CampaignDetailsUiState(
                        campaign = campaign,
                        members = members,
                        isLoading = false,
                        // If the campaign no longer exists, provide an error
                        // instead of displaying an empty details screen.
                        errorMessage = if (campaign == null) {
                            "Campaign not found."
                        } else {
                            null
                        }
                    )
                }.collect { state ->
                    _detailsUiState.value = state
                }
            } catch (exception: CancellationException) {
                // Re-throw cancellation so coroutine cancellation continues
                // to work correctly instead of being treated as an error.
                throw exception
            } catch (exception: Exception) {
                // Store any loading errors so the screen can display them.
                _detailsUiState.value = CampaignDetailsUiState(
                    isLoading = false,
                    errorMessage = exception.message
                        ?: "Unable to load campaign details."
                )
            }
        }
    }

    /**
     * Retrieves profiles that are available to join the specified campaign.
     */
    fun loadAvailableProfiles(campaignId: String) {
        // Cancel any previous collection before loading another campaign's profiles.
        availableProfilesJob?.cancel()

        availableProfilesJob = viewModelScope.launch {
            _availableProfilesUiState.value =
                AvailableCampaignProfilesUiState(isLoading = true)

            try {
                campaignMembershipRepository
                    .getProfilesAvailableForCampaign(campaignId)
                    .collect { profiles ->
                        _availableProfilesUiState.value =
                            AvailableCampaignProfilesUiState(
                                profiles = profiles,
                                isLoading = false
                            )
                    }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _availableProfilesUiState.value =
                    AvailableCampaignProfilesUiState(
                        isLoading = false,
                        errorMessage = exception.message
                            ?: "Unable to load available profiles."
                    )
            }
        }
    }

    /**
     * Loads the active character assignments for a campaign and identifies
     * the assignment belonging to the currently selected profile.
     */
    fun loadCampaignCharacterAssignments(campaignId: String) {
        // Cancel the previous collection so assignments from an older
        // campaign are not collected alongside the new campaign.
        characterAssignmentsJob?.cancel()

        characterAssignmentsJob = viewModelScope.launch {
            _characterAssignmentsUiState.value =
                CampaignCharacterAssignmentsUiState()

            try {
                // Combine campaign assignments with the active profile ID.
                // This allows the screen to identify the current user's assignment.
                combine(
                    campaignCharacterRepository.getActiveCampaignCharacters(campaignId),
                    activeProfileDataStore.activeProfileId
                ) { assignments, activeProfileId ->
                    CampaignCharacterAssignmentsUiState(
                        assignments = assignments,
                        myAssignment = assignments.firstOrNull {
                            it.userId == activeProfileId
                        },
                        activeProfileId = activeProfileId,
                        isLoading = false
                    )
                }.collect { state ->
                    _characterAssignmentsUiState.value = state
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _characterAssignmentsUiState.value =
                    CampaignCharacterAssignmentsUiState(
                        isLoading = false,
                        errorMessage = exception.message
                            ?: "Unable to load campaign characters."
                    )
            }
        }
    }

    /**
     * Assigns one of the active profile's saved characters to a campaign.
     */
    fun assignCharacterToCampaign(
        campaignId: String,
        characterId: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _characterAssignmentOperationUiState.value =
                CharacterAssignmentOperationUiState(
                    isProcessing = true
                )

            try {
                // Retrieve the currently selected profile ID.
                val activeProfileId =
                    activeProfileDataStore.activeProfileId.first()

                // An assignment cannot be made without an active profile.
                require(!activeProfileId.isNullOrBlank()) {
                    "No active profile is selected."
                }

                // The repository handles the assignment and its validation.
                campaignCharacterRepository.assignCharacter(
                    campaignId = campaignId,
                    characterId = characterId,
                    userId = activeProfileId
                )

                // Reset the operation state and notify the screen of success.
                _characterAssignmentOperationUiState.value =
                    CharacterAssignmentOperationUiState()

                onSuccess()
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _characterAssignmentOperationUiState.value =
                    CharacterAssignmentOperationUiState(
                        errorMessage = exception.message
                            ?: "Unable to assign character. Please try again."
                    )
            }
        }
    }

    /**
     * Removes the active profile's character assignment from a campaign.
     */
    fun leaveCharacterInCampaign(
        campaignId: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _characterAssignmentOperationUiState.value =
                CharacterAssignmentOperationUiState(
                    isProcessing = true
                )

            try {
                val activeProfileId =
                    activeProfileDataStore.activeProfileId.first()

                require(!activeProfileId.isNullOrBlank()) {
                    "No active profile is selected."
                }

                // The repository marks the profile's campaign assignment as inactive.
                campaignCharacterRepository.leaveCampaign(
                    campaignId = campaignId,
                    userId = activeProfileId
                )

                _characterAssignmentOperationUiState.value =
                    CharacterAssignmentOperationUiState()

                onSuccess()
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _characterAssignmentOperationUiState.value =
                    CharacterAssignmentOperationUiState(
                        errorMessage = exception.message
                            ?: "Unable to leave character assignment. Please try again."
                    )
            }
        }
    }

    /**
     * Adds another profile to a campaign as a player.
     *
     * The currently active profile is passed as the Dungeon Master
     * so the repository can verify that it has permission to add players.
     */
    fun addPlayerToCampaign(
        campaignId: String,
        playerUserId: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _addPlayerUiState.value = AddCampaignPlayerUiState(
                isAdding = true
            )

            try {
                // The active profile is treated as the Dungeon Master
                // for this operation. The repository verifies its role.
                val dungeonMasterUserId =
                    activeProfileDataStore.activeProfileId.first()

                require(!dungeonMasterUserId.isNullOrBlank()) {
                    "No active profile is selected."
                }

                campaignMembershipRepository.addPlayerToCampaign(
                    campaignId = campaignId,
                    dungeonMasterUserId = dungeonMasterUserId,
                    playerUserId = playerUserId
                )

                _addPlayerUiState.value = AddCampaignPlayerUiState()

                onSuccess()
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _addPlayerUiState.value = AddCampaignPlayerUiState(
                    errorMessage = exception.message
                        ?: "Unable to add player. Please try again."
                )
            }
        }
    }

    /**
     * Creates a new campaign using the currently active profile as its creator.
     */
    fun createCampaign(
        name: String,
        description: String?,
        setting: String?,
        location: String?,
        startingDate: Long,
        maxPlayers: Int,
        onSuccess: (CampaignEntity) -> Unit
    ) {
        viewModelScope.launch {
            _creationUiState.value = CampaignCreationUiState(
                isCreating = true
            )

            try {
                // Retrieve the active profile so it can be recorded
                // as the creator of the new campaign.
                val activeProfileId =
                    activeProfileDataStore.activeProfileId.first()

                require(!activeProfileId.isNullOrBlank()) {
                    "No active profile is selected."
                }

                // Pass the campaign information to the repository.
                // The repository handles saving the campaign and its creator membership.
                val campaign = campaignRepository.createCampaign(
                    name = name,
                    description = description,
                    setting = setting,
                    location = location,
                    startingDate = startingDate,
                    maxPlayers = maxPlayers,
                    createdByUserId = activeProfileId
                )

                _creationUiState.value = CampaignCreationUiState()

                // Return the created campaign to the calling screen.
                onSuccess(campaign)

            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _creationUiState.value = CampaignCreationUiState(
                    errorMessage = exception.message
                        ?: "Unable to create campaign. Please try again."
                )
            }
        }
    }

    /**
     * Updates an existing campaign using the currently active profile
     * as the user requesting the change.
     */
    fun updateCampaign(
        campaignId: String,
        name: String,
        description: String?,
        setting: String?,
        location: String?,
        startingDate: Long,
        maxPlayers: Int,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _updateUiState.value = CampaignUpdateUiState(
                isUpdating = true
            )

            try {
                // Retrieve the active profile ID so the repository
                // can verify that the user has permission to edit the campaign.
                val activeProfileId =
                    activeProfileDataStore.activeProfileId.first()

                require(!activeProfileId.isNullOrBlank()) {
                    "No active profile is selected."
                }

                campaignRepository.updateCampaign(
                    campaignId = campaignId,
                    editorUserId = activeProfileId,
                    name = name,
                    description = description,
                    setting = setting,
                    location = location,
                    startingDate = startingDate,
                    maxPlayers = maxPlayers
                )

                _updateUiState.value = CampaignUpdateUiState()

                onSuccess()

            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _updateUiState.value = CampaignUpdateUiState(
                    errorMessage = exception.message
                        ?: "Unable to update campaign. Please try again."
                )
            }
        }
    }
}