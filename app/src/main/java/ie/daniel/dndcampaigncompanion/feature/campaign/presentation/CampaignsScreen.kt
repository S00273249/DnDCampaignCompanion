package ie.daniel.dndcampaigncompanion.feature.campaign.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ie.daniel.dndcampaigncompanion.feature.profile.presentation.ProfileViewModel

/**
 * Displays the campaigns associated with the currently selected profile.
 *
 * Shows the user's role and campaign information, and provides navigation
 * to campaign details, character management and profile management.
 */
@Composable
fun CampaignsScreen(
    campaignViewModel: CampaignViewModel,
    profileViewModel: ProfileViewModel,
    onManageProfiles: () -> Unit,
    onCreateCampaign: () -> Unit,
    onCampaignSelected: (String) -> Unit,
    onOpenCharacters: () -> Unit
) {
    // Observes campaign and profile information from their respective ViewModels.
    val campaignState by campaignViewModel.uiState.collectAsState()
    val profileState by profileViewModel.uiState.collectAsState()

    // Finds the profile currently selected in the campaign state.
    // This allows the screen to show which profile is being used.
    val activeProfile = profileState.profiles.find {
        it.id == campaignState.activeProfileId
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Campaigns",
            style = MaterialTheme.typography.headlineMedium
        )

        // Displays the username of the currently active profile when available.
        if (activeProfile != null) {
            Text(
                text = "Playing as ${activeProfile.username}",
                style = MaterialTheme.typography.titleMedium
            )
        }

        // Displays the appropriate content depending on the campaign loading state.
        when {
            campaignState.isLoading -> {
                // Shows a loading indicator while campaign data is being retrieved.
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            campaignState.campaigns.isEmpty() -> {
                // Shown when the active profile has no campaigns associated with it.
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "You haven't joined any campaigns yet.",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Text(
                        text = "Create a campaign or join one to get started.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            else -> {
                // Displays the campaigns in a scrollable list.
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Each campaign is identified by its unique ID.
                    // Providing a key helps Compose keep track of list items
                    // when the campaign list changes.
                    items(
                        items = campaignState.campaigns,
                        key = { item -> item.campaign.id }
                    ) { item ->
                        // Each card represents a campaign and opens its details
                        // when the user selects it.
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onCampaignSelected(item.campaign.id)
                                }
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = item.campaign.name,
                                    style = MaterialTheme.typography.titleMedium
                                )

                                // Shows whether the active profile is the DM or a player.
                                Text(
                                    text = "Role: ${item.membership.role.name}",
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                // Displays the campaign's current status.
                                Text(
                                    text = "Status: ${item.campaign.status.name}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                // Campaign descriptions are optional, so this
                                // section is only displayed when one exists.
                                item.campaign.description?.let { description ->
                                    Text(
                                        text = description,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }

                                Text(
                                    text = "Tap to view campaign",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Opens the campaign creation form.
        Button(
            onClick = onCreateCampaign,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Create Campaign")
        }

        // Opens the user's saved characters.
        Button(
            onClick = onOpenCharacters,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("My Characters")
        }

        // Allows the user to switch or manage their saved profiles.
        Button(
            onClick = onManageProfiles,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Manage Profiles")
        }
    }
}