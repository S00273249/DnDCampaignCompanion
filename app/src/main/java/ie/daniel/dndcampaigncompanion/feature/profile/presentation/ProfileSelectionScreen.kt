package ie.daniel.dndcampaigncompanion.feature.profile.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
fun ProfileSelectionScreen(
    viewModel: ProfileViewModel,
    onProfileSelected: () -> Unit,
    onCreateProfile: () -> Unit
) {
    // Observe the available profiles and currently selected profile from the ViewModel.
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Select Profile",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Choose a profile to continue or create a new one.",
            style = MaterialTheme.typography.bodyLarge
        )

        // Show a loading indicator while the profile list is being retrieved.
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.profiles.isEmpty()) {
            // Display a message when no profiles have been created on this device.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No profiles have been created yet.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            // Display the available profiles in a scrollable list.
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Create one selectable card for each profile.
                // The profile ID provides a stable key for Compose.
                items(
                    items = uiState.profiles,
                    key = { profile -> profile.id }
                ) { profile ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                // Save this profile as the active profile.
                                viewModel.selectProfile(profile.id)

                                // Notify the navigation layer that a profile was selected.
                                onProfileSelected()
                            }
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = profile.username,
                                style = MaterialTheme.typography.titleMedium
                            )

                            // Indicate whether this profile is already selected.
                            Text(
                                text = if (profile.id == uiState.activeProfileId) {
                                    "Currently selected"
                                } else {
                                    "Tap to select"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
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
    }
}