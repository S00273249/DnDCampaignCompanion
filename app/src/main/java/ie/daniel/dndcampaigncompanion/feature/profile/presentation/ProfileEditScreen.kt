package ie.daniel.dndcampaigncompanion.feature.profile.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ProfileEditScreen(
    viewModel: ProfileViewModel,
    profileId: String,
    onSaved: () -> Unit,
    onCancel: () -> Unit
) {
    // Observe the current profile list from the ViewModel.
    val uiState by viewModel.uiState.collectAsState()

    // Find the profile that matches the ID passed into this screen.
    val profile = uiState.profiles.find {
        it.id == profileId
    }

    // If the profile cannot be found, show a message instead of the edit form.
    if (profile == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Profile not found.",
                style = MaterialTheme.typography.bodyLarge
            )

            OutlinedButton(onClick = onCancel) {
                Text("Back")
            }
        }

        return
    }

    // Use the profile ID as the key so Compose creates fresh form state
    // if the user navigates to a different profile.
    key(profile.id) {
        // Start the username field with the profile's existing username.
        var username by remember {
            mutableStateOf(profile.username)
        }

        // Store validation feedback for the username field.
        var errorMessage by remember {
            mutableStateOf<String?>(null)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Edit Profile",
                style = MaterialTheme.typography.headlineMedium
            )

            OutlinedTextField(
                value = username,
                onValueChange = {
                    username = it

                    // Clear the previous validation error when the user edits the field.
                    errorMessage = null
                },
                label = {
                    Text("Username")
                },
                singleLine = true,
                isError = errorMessage != null,
                // Display validation feedback underneath the username field.
                supportingText = {
                    errorMessage?.let {
                        Text(it)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    // Remove leading and trailing whitespace before validating the username.
                    val trimmedUsername = username.trim()

                    // Check that the username is not blank and does not exceed 30 characters.
                    when {
                        trimmedUsername.isBlank() -> {
                            errorMessage = "Please enter a username."
                        }

                        trimmedUsername.length > 30 -> {
                            errorMessage =
                                "Username must be 30 characters or fewer."
                        }

                        else -> {
                            // Ask the ViewModel to update the selected profile's username.
                            viewModel.updateUsername(
                                profileId = profileId,
                                username = trimmedUsername
                            )

                            // Return to the previous screen after requesting the update.
                            onSaved()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Changes")
            }

            // Return to the previous screen without saving.
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancel")
            }
        }
    }
}