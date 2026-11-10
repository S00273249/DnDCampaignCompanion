package ie.daniel.dndcampaigncompanion.feature.profile.presentation

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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ProfileManagementScreen(
    viewModel: ProfileViewModel,
    onSwitchProfile: (String) -> Unit,
    onEditProfile: (String) -> Unit,
    onCreateProfile: () -> Unit,
    onBack: () -> Unit
) {
    // Observe the profile list and currently active profile from the ViewModel.
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Manage Profiles",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Manage the profiles stored on this device.",
            style = MaterialTheme.typography.bodyLarge
        )

        // Display different content depending on the current profile loading state.
        when {
            // Show a loading indicator while the profile list is being retrieved.
            uiState.isLoading -> {
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

            // Show a message when there are no profiles stored on the device.
            uiState.profiles.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("No profiles have been created yet.")
                }
            }

            else -> {
                // LazyColumn only composes the profile cards that are currently needed on screen.
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Create one list item for each saved profile.
                    // The profile ID provides a stable key for Compose.
                    items(
                        items = uiState.profiles,
                        key = { profile -> profile.id }
                    ) { profile ->
                        // Check whether this profile is the one currently selected on the device.
                        val isActive =
                            profile.id == uiState.activeProfileId

                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = profile.username,
                                    style = MaterialTheme.typography.titleMedium
                                )

                                // Display the profile's current activation status.
                                Text(
                                    text = if (isActive) {
                                        "Currently active"
                                    } else {
                                        "Not currently active"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Open the editing screen for this profile.
                                    OutlinedButton(
                                        onClick = {
                                            onEditProfile(profile.id)
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Edit")
                                    }

                                    // Only show the switch button for profiles that are not already active.
                                    if (!isActive) {
                                        Button(
                                            onClick = {
                                                onSwitchProfile(profile.id)
                                            },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Switch")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Navigate to the profile creation screen.
        Button(
            onClick = onCreateProfile,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Create New Profile")
        }

        // Return to the previous screen.
        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back")
        }
    }
}