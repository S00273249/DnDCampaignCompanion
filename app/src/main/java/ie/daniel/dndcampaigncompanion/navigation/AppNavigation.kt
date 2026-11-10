package ie.daniel.dndcampaigncompanion.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import ie.daniel.dndcampaigncompanion.feature.campaign.presentation.CampaignCreationScreen
import ie.daniel.dndcampaigncompanion.feature.campaign.presentation.CampaignDetailsScreen
import ie.daniel.dndcampaigncompanion.feature.campaign.presentation.CampaignEditScreen
import ie.daniel.dndcampaigncompanion.feature.campaign.presentation.CampaignViewModel
import ie.daniel.dndcampaigncompanion.feature.campaign.presentation.CampaignsScreen
import ie.daniel.dndcampaigncompanion.feature.campaign.presentation.CharacterAssignmentScreen
import ie.daniel.dndcampaigncompanion.feature.campaign.presentation.ManageCampaignMembersScreen
import ie.daniel.dndcampaigncompanion.feature.character.presentation.CharacterCreationScreen
import ie.daniel.dndcampaigncompanion.feature.character.presentation.CharacterDetailsScreen
import ie.daniel.dndcampaigncompanion.feature.character.presentation.CharacterEditScreen
import ie.daniel.dndcampaigncompanion.feature.character.presentation.CharacterListScreen
import ie.daniel.dndcampaigncompanion.feature.character.presentation.CharacterViewModel
import ie.daniel.dndcampaigncompanion.feature.dice.presentation.DiceRollScreen
import ie.daniel.dndcampaigncompanion.feature.dice.presentation.DiceRollViewModel
import ie.daniel.dndcampaigncompanion.feature.profile.presentation.ProfileCreationScreen
import ie.daniel.dndcampaigncompanion.feature.profile.presentation.ProfileEditScreen
import ie.daniel.dndcampaigncompanion.feature.profile.presentation.ProfileManagementScreen
import ie.daniel.dndcampaigncompanion.feature.profile.presentation.ProfileSelectionScreen
import ie.daniel.dndcampaigncompanion.feature.profile.presentation.ProfileViewModel

/**
 * Sets up navigation between the different screens in the application.
 *
 * Receives the shared ViewModels and NavController so that screens can
 * access application data and navigate to other destinations.
 */
@Composable
fun AppNavigation(
    navController: NavHostController,
    diceRollViewModel: DiceRollViewModel,
    profileViewModel: ProfileViewModel,
    campaignViewModel: CampaignViewModel,
    characterViewModel: CharacterViewModel,
    startDestination: String = Screen.PROFILE_SELECTION,
    modifier: Modifier = Modifier
) {
    // NavHost connects each route to the Composable screen it should display.
    // The supplied start destination determines which screen opens first.
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier.fillMaxSize()
    ) {
        // Displays existing profiles and allows the user to create a new one.
        composable(Screen.PROFILE_SELECTION) {
            ProfileSelectionScreen(
                viewModel = profileViewModel,
                onProfileSelected = {
                    navController.navigate(Screen.CAMPAIGNS) {
                        // Remove profile selection from the back stack so that
                        // pressing Back does not return to the selection screen.
                        popUpTo(Screen.PROFILE_SELECTION) {
                            inclusive = true
                        }
                    }
                },
                onCreateProfile = {
                    navController.navigate(Screen.PROFILE_CREATION)
                }
            )
        }

        // Displays the profile creation form.
        composable(Screen.PROFILE_CREATION) {
            ProfileCreationScreen(
                viewModel = profileViewModel,
                onProfileCreated = {
                    // If profile creation was opened from profile management,
                    // return there instead of navigating to the campaigns screen.
                    if (
                        navController.previousBackStackEntry
                            ?.destination?.route == Screen.PROFILE_MANAGEMENT
                    ) {
                        navController.popBackStack()
                    } else {
                        // Otherwise, continue to the campaigns screen and remove
                        // profile selection from the back stack.
                        navController.navigate(Screen.CAMPAIGNS) {
                            popUpTo(Screen.PROFILE_SELECTION) {
                                inclusive = true
                            }
                        }
                    }
                },
                onCancel = {
                    navController.popBackStack()
                }
            )
        }

        // The profile ID is supplied as a navigation argument so the correct
        // profile can be loaded for editing.
        composable(
            route = Screen.PROFILE_EDIT,
            arguments = listOf(
                navArgument("profileId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val profileId =
                backStackEntry.arguments?.getString("profileId")

            if (profileId != null) {
                ProfileEditScreen(
                    viewModel = profileViewModel,
                    profileId = profileId,
                    onSaved = {
                        navController.popBackStack()
                    },
                    onCancel = {
                        navController.popBackStack()
                    }
                )
            } else {
                // Displayed if the expected profile ID was not provided.
                PlaceholderScreen("Profile not found")
            }
        }

        // Allows the user to manage saved profiles and switch the active profile.
        composable(Screen.PROFILE_MANAGEMENT) {
            ProfileManagementScreen(
                viewModel = profileViewModel,
                onSwitchProfile = { profileId ->
                    // Updates the active profile before returning to campaigns.
                    profileViewModel.selectProfile(profileId)

                    navController.navigate(Screen.CAMPAIGNS) {
                        // Remove profile management so Back does not return to it.
                        popUpTo(Screen.PROFILE_MANAGEMENT) {
                            inclusive = true
                        }
                    }
                },
                onEditProfile = { profileId ->
                    navController.navigate(
                        Screen.profileEdit(profileId)
                    )
                },
                onCreateProfile = {
                    navController.navigate(Screen.PROFILE_CREATION)
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // Main campaign list for the currently selected profile.
        composable(Screen.CAMPAIGNS) {
            CampaignsScreen(
                campaignViewModel = campaignViewModel,
                profileViewModel = profileViewModel,
                onManageProfiles = {
                    navController.navigate(Screen.PROFILE_MANAGEMENT)
                },
                onCreateCampaign = {
                    navController.navigate(Screen.CAMPAIGN_CREATION)
                },
                onCampaignSelected = { campaignId ->
                    // Opens the selected campaign's details using its ID.
                    navController.navigate(
                        Screen.campaignDetails(campaignId)
                    )
                },
                onOpenCharacters = {
                    navController.navigate(Screen.CHARACTERS)
                }
            )
        }

        // Displays the form for creating a new campaign.
        composable(Screen.CAMPAIGN_CREATION) {
            CampaignCreationScreen(
                campaignViewModel = campaignViewModel,
                onCampaignCreated = {
                    navController.popBackStack()
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // Opens the details of a specific campaign.
        composable(
            route = Screen.CAMPAIGN_DETAILS,
            arguments = listOf(
                navArgument("campaignId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val campaignId =
                backStackEntry.arguments?.getString("campaignId")

            if (campaignId != null) {
                CampaignDetailsScreen(
                    campaignViewModel = campaignViewModel,
                    campaignId = campaignId,
                    onBack = {
                        navController.popBackStack()
                    },
                    onEditCampaign = { selectedCampaignId ->
                        navController.navigate(
                            Screen.campaignEdit(selectedCampaignId)
                        )
                    },
                    onManageMembers = { selectedCampaignId ->
                        navController.navigate(
                            Screen.campaignMemberManagement(selectedCampaignId)
                        )
                    },
                    onManageCharacter = { selectedCampaignId ->
                        navController.navigate(
                            Screen.campaignCharacterAssignment(selectedCampaignId)
                        )
                    }
                )
            } else {
                PlaceholderScreen("Campaign not found")
            }
        }

        // Opens the edit screen for a specific campaign.
        composable(
            route = Screen.CAMPAIGN_EDIT,
            arguments = listOf(
                navArgument("campaignId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val campaignId =
                backStackEntry.arguments?.getString("campaignId")

            if (campaignId != null) {
                CampaignEditScreen(
                    campaignId = campaignId,
                    campaignViewModel = campaignViewModel,
                    onCampaignUpdated = {
                        navController.popBackStack()
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            } else {
                PlaceholderScreen("Campaign not found")
            }
        }

        // Allows the Dungeon Master to manage the members of a campaign.
        composable(
            route = Screen.CAMPAIGN_MEMBER_MANAGEMENT,
            arguments = listOf(
                navArgument("campaignId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val campaignId =
                backStackEntry.arguments?.getString("campaignId")

            if (campaignId != null) {
                ManageCampaignMembersScreen(
                    campaignViewModel = campaignViewModel,
                    campaignId = campaignId,
                    onBack = {
                        navController.popBackStack()
                    }
                )
            } else {
                PlaceholderScreen("Campaign not found")
            }
        }

        // Allows campaign characters to be assigned to campaign members.
        composable(
            route = Screen.CAMPAIGN_CHARACTER_ASSIGNMENT,
            arguments = listOf(
                navArgument("campaignId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val campaignId =
                backStackEntry.arguments?.getString("campaignId")

            if (campaignId != null) {
                CharacterAssignmentScreen(
                    campaignViewModel = campaignViewModel,
                    campaignId = campaignId,
                    onBack = {
                        navController.popBackStack()
                    }
                )
            } else {
                PlaceholderScreen("Campaign not found")
            }
        }

        // Displays the saved characters belonging to the active profile.
        composable(Screen.CHARACTERS) {
            CharacterListScreen(
                viewModel = characterViewModel,
                onCreateCharacter = {
                    navController.navigate(Screen.CHARACTER_CREATION)
                },
                onCharacterSelected = { characterId ->
                    navController.navigate(
                        Screen.characterDetails(characterId)
                    )
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // Displays the form for creating a new character.
        composable(Screen.CHARACTER_CREATION) {
            CharacterCreationScreen(
                viewModel = characterViewModel,
                onCharacterCreated = {
                    navController.popBackStack()
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // Displays the details of a specific character.
        composable(
            route = Screen.CHARACTER_DETAILS,
            arguments = listOf(
                navArgument("characterId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val characterId =
                backStackEntry.arguments?.getString("characterId")

            if (characterId != null) {
                CharacterDetailsScreen(
                    viewModel = characterViewModel,
                    characterId = characterId,
                    onEditCharacter = { selectedCharacterId ->
                        navController.navigate(
                            Screen.characterEdit(selectedCharacterId)
                        )
                    },
                    onCharacterDeleted = {
                        navController.popBackStack()
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            } else {
                PlaceholderScreen("Character not found")
            }
        }

        // Opens the edit screen for a specific character.
        composable(
            route = Screen.CHARACTER_EDIT,
            arguments = listOf(
                navArgument("characterId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val characterId =
                backStackEntry.arguments?.getString("characterId")

            if (characterId != null) {
                CharacterEditScreen(
                    viewModel = characterViewModel,
                    characterId = characterId,
                    onCharacterUpdated = {
                        navController.popBackStack()
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            } else {
                PlaceholderScreen("Character not found")
            }
        }

        // Session screens are currently placeholders.
        composable(Screen.SESSIONS) {
            PlaceholderScreen("Sessions")
        }

        composable(Screen.SESSION_DETAILS) {
            PlaceholderScreen("Session Details")
        }

        // Opens the dice roller using the shared DiceRollViewModel.
        composable(Screen.DICE_ROLLER) {
            DiceRollScreen(
                viewModel = diceRollViewModel
            )
        }

        // Campaign gallery is not implemented yet.
        composable(Screen.CAMPAIGN_GALLERY) {
            PlaceholderScreen("Campaign Gallery")
        }
    }
}

/**
 * A simple reusable screen shown for navigation destinations
 * that have not been implemented yet, or when a required ID is missing.
 */
@Composable
private fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = title)
    }
}
