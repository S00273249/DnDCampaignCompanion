package ie.daniel.dndcampaigncompanion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import ie.daniel.dndcampaigncompanion.feature.campaign.presentation.CampaignViewModel
import ie.daniel.dndcampaigncompanion.feature.campaign.presentation.CampaignViewModelFactory
import ie.daniel.dndcampaigncompanion.feature.character.presentation.CharacterViewModel
import ie.daniel.dndcampaigncompanion.feature.character.presentation.CharacterViewModelFactory
import ie.daniel.dndcampaigncompanion.feature.dice.presentation.DiceRollViewModel
import ie.daniel.dndcampaigncompanion.feature.profile.presentation.ProfileViewModel
import ie.daniel.dndcampaigncompanion.feature.profile.presentation.ProfileViewModelFactory
import ie.daniel.dndcampaigncompanion.navigation.AppNavigation
import ie.daniel.dndcampaigncompanion.navigation.Screen
import ie.daniel.dndcampaigncompanion.ui.theme.DnDCampaignCompanionTheme

/**
 * Main entry point of the application.
 *
 * Sets up the Jetpack Compose interface, creates the required ViewModels
 * and controls the initial navigation based on the user's active profile.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Allows the application content to draw behind system bars.
        // The UI can then handle system bar insets to use the available screen space.
        enableEdgeToEdge()

        // Retrieves the shared AppContainer from the custom Application class.
        // This provides access to the repositories and DataStore used by the ViewModels.
        val appContainer =
            (application as DndCampaignApplication).appContainer

        // Sets up the Jetpack Compose interface using the application's theme.
        setContent {
            DnDCampaignCompanionTheme {

                // Creates and remembers the navigation controller.
                // This manages navigation between the application's screens.
                val navController = rememberNavController()

                // Creates the dice roller ViewModel.
                val diceRollViewModel: DiceRollViewModel = viewModel()

                // Creates the profile ViewModel and provides the dependencies
                // it needs to manage user profiles and the active profile selection.
                val profileViewModel: ProfileViewModel = viewModel(
                    factory = ProfileViewModelFactory(
                        userProfileRepository =
                            appContainer.userProfileRepository,
                        activeProfileDataStore =
                            appContainer.activeProfileDataStore
                    )
                )

                // Creates the campaign ViewModel and provides the repositories
                // and DataStore needed to manage campaigns, memberships and characters.
                val campaignViewModel: CampaignViewModel = viewModel(
                    factory = CampaignViewModelFactory(
                        campaignRepository =
                            appContainer.campaignRepository,
                        campaignMembershipRepository =
                            appContainer.campaignMembershipRepository,
                        activeProfileDataStore =
                            appContainer.activeProfileDataStore,
                        userProfileRepository =
                            appContainer.userProfileRepository,
                        campaignCharacterRepository =
                            appContainer.campaignCharacterRepository,
                        characterRepository =
                            appContainer.characterRepository
                    )
                )

                // Creates the character ViewModel and provides access to saved characters
                // and the currently selected user profile.
                val characterViewModel: CharacterViewModel = viewModel(
                    factory = CharacterViewModelFactory(
                        characterRepository =
                            appContainer.characterRepository,
                        activeProfileDataStore =
                            appContainer.activeProfileDataStore
                    )
                )

                // Observes the profile ViewModel's UI state.
                // The by keyword allows the current state to be accessed directly
                // and automatically updated when the ViewModel emits new values.
                val profileUiState by
                profileViewModel.uiState.collectAsState()

                // Runs whenever the active profile ID, available profiles
                // or loading state changes.
                // This keeps the stored active profile selection valid.
                LaunchedEffect(
                    profileUiState.activeProfileId,
                    profileUiState.profiles,
                    profileUiState.isLoading
                ) {
                    val activeProfileId =
                        profileUiState.activeProfileId

                    // Checks whether the selected profile still exists.
                    // If loading has finished but the selected profile cannot
                    // be found, the active profile selection is cleared.
                    if (
                        !profileUiState.isLoading &&
                        activeProfileId != null &&
                        profileUiState.profiles.none {
                            it.id == activeProfileId
                        }
                    ) {
                        profileViewModel.clearActiveProfile()
                    }
                }

                // Scaffold provides the main layout structure for the application
                // and supplies the padding needed to account for system bars.
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->

                    // Displays a loading indicator while the profile information
                    // is being retrieved from the database and DataStore.
                    if (profileUiState.isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else {

                        // Checks whether the selected active profile exists
                        // in the list of profiles retrieved by the ViewModel.
                        val hasValidActiveProfile =
                            profileUiState.profiles.any {
                                it.id == profileUiState.activeProfileId
                            }

                        // Determines which screen should be displayed first.
                        // Users with a valid active profile go to their campaigns.
                        // Otherwise, they are taken to profile selection.
                        val startDestination =
                            if (hasValidActiveProfile) {
                                Screen.CAMPAIGNS
                            } else {
                                Screen.PROFILE_SELECTION
                            }

                        // Sets up the application's navigation graph.
                        // The shared ViewModels are passed into navigation so
                        // the relevant screens can access their data and operations.
                        AppNavigation(
                            navController = navController,
                            diceRollViewModel = diceRollViewModel,
                            profileViewModel = profileViewModel,
                            campaignViewModel = campaignViewModel,
                            characterViewModel = characterViewModel,
                            startDestination = startDestination,
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}
