package ie.daniel.dndcampaigncompanion.feature.campaign.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ie.daniel.dndcampaigncompanion.data.local.ActiveProfileDataStore
import ie.daniel.dndcampaigncompanion.data.repository.CampaignCharacterRepository
import ie.daniel.dndcampaigncompanion.data.repository.CampaignMembershipRepository
import ie.daniel.dndcampaigncompanion.data.repository.CampaignRepository
import ie.daniel.dndcampaigncompanion.data.repository.CharacterRepository
import ie.daniel.dndcampaigncompanion.data.repository.UserProfileRepository

/**
 * Creates CampaignViewModel instances with their required dependencies.
 *
 * Used when the ViewModel cannot be created using the default constructor
 * because it requires repositories and other dependencies.
 */
class CampaignViewModelFactory(
    private val campaignRepository: CampaignRepository,
    private val campaignMembershipRepository: CampaignMembershipRepository,
    private val activeProfileDataStore: ActiveProfileDataStore,
    private val userProfileRepository: UserProfileRepository,
    private val campaignCharacterRepository: CampaignCharacterRepository,
    private val characterRepository: CharacterRepository
) : ViewModelProvider.Factory {

    // Called by ViewModelProvider when a ViewModel instance is requested.
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        // Check whether the requested ViewModel is CampaignViewModel
        // before attempting to create an instance of it.
        if (modelClass.isAssignableFrom(CampaignViewModel::class.java)) {
            // The factory returns the requested generic ViewModel type.
            // The cast is unchecked because Kotlin cannot verify the generic
            // type at runtime, but the class check above ensures it is correct.
            @Suppress("UNCHECKED_CAST")
            return CampaignViewModel(
                campaignRepository = campaignRepository,
                campaignMembershipRepository = campaignMembershipRepository,
                activeProfileDataStore = activeProfileDataStore,
                userProfileRepository = userProfileRepository,
                campaignCharacterRepository = campaignCharacterRepository,
                characterRepository = characterRepository
            ) as T
        }

        // Prevents the factory from silently creating an unsupported ViewModel type.
        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}