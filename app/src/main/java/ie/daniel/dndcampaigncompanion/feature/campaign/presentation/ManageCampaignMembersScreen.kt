package ie.daniel.dndcampaigncompanion.feature.campaign.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ManageCampaignMembersScreen(
    campaignViewModel: CampaignViewModel,
    campaignId: String,
    onBack: () -> Unit
) {
    // Collect profiles that are eligible to join this campaign.
    val profilesState by campaignViewModel.availableProfilesUiState.collectAsState()

    // Collect the state of attempts to add players, including loading and error information.
    val addPlayerState by campaignViewModel.addPlayerUiState.collectAsState()

    // Load the profiles available to join this campaign whenever its ID changes.
    LaunchedEffect(campaignId) {
        campaignViewModel.loadAvailableProfiles(campaignId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Return to the campaign screen.
        Button(
            onClick = onBack
        ) {
            Text("Back to Campaign")
        }

        Text(
            text = "Add Players",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Select a local profile to add to this campaign as a Player.",
            style = MaterialTheme.typography.bodyLarge
        )

        // Display errors from an attempt to add a player.
        addPlayerState.errorMessage?.let { errorMessage ->
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // Show the appropriate content based on the profile loading state.
        when {
            profilesState.isLoading -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                    Text("Loading available profiles...")
                }
            }

            profilesState.errorMessage != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = profilesState.errorMessage
                            ?: "Unable to load profiles.",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            profilesState.profiles.isEmpty() -> {
                // An empty result means there are currently no profiles
                // available to add to this campaign.
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "No available profiles",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Text(
                        text = "All existing profiles are already members of this campaign.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            else -> {
                // Display each available profile in a scrollable list.
                // The profile ID provides a stable key for Compose.
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = profilesState.profiles,
                        key = { profile -> profile.id }
                    ) { profile ->
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    // Show the profile's username and identify it
                                    // as a locally stored profile.
                                    Text(
                                        text = profile.username,
                                        style = MaterialTheme.typography.titleMedium
                                    )

                                    Text(
                                        text = "Local profile",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }

                                // Add this profile to the campaign as a player.
                                // Disable the button while an add operation is running.
                                Button(
                                    onClick = {
                                        campaignViewModel.addPlayerToCampaign(
                                            campaignId = campaignId,
                                            playerUserId = profile.id,
                                            onSuccess = {}
                                        )
                                    },
                                    enabled = !addPlayerState.isAdding
                                ) {
                                    Text(
                                        if (addPlayerState.isAdding) {
                                            "Adding..."
                                        } else {
                                            "Add Player"
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}