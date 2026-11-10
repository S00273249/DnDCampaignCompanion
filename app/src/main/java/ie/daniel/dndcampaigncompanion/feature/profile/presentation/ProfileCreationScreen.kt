package ie.daniel.dndcampaigncompanion.feature.profile.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ProfileCreationScreen(
    viewModel: ProfileViewModel,
    onProfileCreated: () -> Unit,
    onCancel: () -> Unit
) {
    // Store the username entered by the user.
    var username by remember { mutableStateOf("") }

    // Keep form validation errors separate from errors returned by the ViewModel.
    var validationError by remember { mutableStateOf<String?>(null) }

    // Observe the profile creation state to track progress and display errors.
    val creationState by viewModel.creationUiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Create Profile",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Choose a username for your local profile.",
            style = MaterialTheme.typography.bodyLarge
        )

        // Allow the user to enter a username.
        // Editing the field clears any previous validation error.
        OutlinedTextField(
            value = username,
            onValueChange = {
                username = it
                validationError = null
            },
            label = {
                Text("Username")
            },
            placeholder = {
                Text("Enter your username")
            },
            singleLine = true,
            enabled = !creationState.isCreating,
            isError = validationError != null,
            // Show validation feedback underneath the input field.
            supportingText = {
                validationError?.let { message ->
                    Text(message)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        // Display any error returned by the profile creation operation.
        creationState.errorMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Button(
            onClick = {
                // Remove leading and trailing whitespace before validating the username.
                val trimmedUsername = username.trim()

                // Check that the username contains text and does not exceed 30 characters.
                if (trimmedUsername.isBlank()) {
                    validationError = "Please enter a username."
                } else if (trimmedUsername.length > 30) {
                    validationError = "Username must be 30 characters or fewer."
                } else {
                    // Pass the validated username to the ViewModel.
                    // The callback runs when profile creation succeeds.
                    viewModel.createProfile(
                        username = trimmedUsername,
                        onSuccess = onProfileCreated
                    )
                }
            },
            enabled = !creationState.isCreating,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Replace the button text with a progress indicator while creating the profile.
            if (creationState.isCreating) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(4.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text("Create Profile")
            }
        }

        // Allow the user to cancel without creating a profile.
        // Prevent cancellation while the creation operation is running.
        OutlinedButton(
            onClick = onCancel,
            enabled = !creationState.isCreating,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cancel")
        }
    }
}