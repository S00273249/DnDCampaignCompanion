package ie.daniel.dndcampaigncompanion.feature.campaign.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ie.daniel.dndcampaigncompanion.data.local.entity.CharacterEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterAssignmentScreen(
    campaignViewModel: CampaignViewModel,
    campaignId: String,
    onBack: () -> Unit
) {
    // Collect the characters belonging to the active profile.
    val availableCharactersState by
    campaignViewModel.availableCharactersUiState.collectAsState()

    // Collect the current character assignments for this campaign.
    val assignmentsState by
    campaignViewModel.characterAssignmentsUiState.collectAsState()

    // Collect the state of operations such as assigning or leaving a character.
    val operationState by
    campaignViewModel.characterAssignmentOperationUiState.collectAsState()

    // Remember whether the leave confirmation dialog is currently visible.
    var showLeaveConfirmation by rememberSaveable {
        mutableStateOf(false)
    }

    // Load the campaign's character assignments whenever the campaign ID changes.
    LaunchedEffect(campaignId) {
        campaignViewModel.loadCampaignCharacterAssignments(campaignId)
    }

    // Find the saved character that matches the user's current campaign assignment.
    // The assignment stores the character ID, while the character details come
    // from the available characters list.
    val assignedCharacter = assignmentsState.myAssignment?.let { assignment ->
        availableCharactersState.characters.firstOrNull {
            it.id == assignment.characterId
        }
    }

    // Exclude the currently assigned character from the list of available choices.
    val availableCharacters = availableCharactersState.characters.filter {
        it.id != assignmentsState.myAssignment?.characterId
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Character Assignment")
                },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Back")
                    }
                }
            )
        }
    ) { innerPadding ->

        // Use a LazyColumn so the character information and available choices
        // can scroll without loading every item into the layout at once.
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Your Campaign Character",
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            item {
                // Show the appropriate content depending on the assignment state.
                when {
                    assignmentsState.isLoading -> {
                        CircularProgressIndicator()
                    }

                    assignmentsState.errorMessage != null -> {
                        Text(
                            text = assignmentsState.errorMessage.orEmpty(),
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    assignmentsState.myAssignment != null -> {
                        // Display the assigned character and their campaign level.
                        // The fallback name is used if the character details
                        // are not currently available.
                        AssignedCharacterCard(
                            assignmentName = assignedCharacter?.name
                                ?: "Assigned character",
                            character = assignedCharacter,
                            level = assignmentsState.myAssignment!!.level,
                            onLeave = {
                                showLeaveConfirmation = true
                            },
                            isProcessing = operationState.isProcessing
                        )
                    }

                    else -> {
                        Text(
                            text = "You haven't assigned a character to this campaign yet.",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            // Only show the available character choices when the user
            // does not already have an assignment in this campaign.
            if (assignmentsState.myAssignment == null) {
                item {
                    Text(
                        text = "Available Characters",
                        style = MaterialTheme.typography.headlineSmall
                    )
                }

                // Handle loading, errors and empty results before displaying
                // the list of characters that can be assigned.
                when {
                    availableCharactersState.isLoading -> {
                        item {
                            CircularProgressIndicator()
                        }
                    }

                    availableCharactersState.errorMessage != null -> {
                        item {
                            Text(
                                text = availableCharactersState.errorMessage.orEmpty(),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    availableCharacters.isEmpty() -> {
                        item {
                            Text(
                                text = "You don't have any available characters. Create a character first, then return here to assign it.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    else -> {
                        // Create one list item for each available character.
                        // The character ID provides a stable key for Compose.
                        items(
                            items = availableCharacters,
                            key = { character -> character.id }
                        ) { character ->
                            AvailableCharacterCard(
                                character = character,
                                isProcessing = operationState.isProcessing,
                                onAssign = {
                                    // Request that the selected character is assigned
                                    // to this campaign through the ViewModel.
                                    campaignViewModel.assignCharacterToCampaign(
                                        campaignId = campaignId,
                                        characterId = character.id,
                                        onSuccess = {}
                                    )
                                }
                            )
                        }
                    }
                }
            }

            // Display errors from assignment or leave operations separately
            // from errors encountered while loading campaign data.
            if (operationState.errorMessage != null) {
                item {
                    Text(
                        text = operationState.errorMessage.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }

    // Ask for confirmation before removing the character from the campaign.
    if (showLeaveConfirmation) {
        AlertDialog(
            onDismissRequest = {
                showLeaveConfirmation = false
            },
            title = {
                Text("Leave character assignment?")
            },
            text = {
                Text(
                    "Your character's campaign progression will be preserved. You can reassign this character later."
                )
            },
            confirmButton = {
                TextButton(
                    enabled = !operationState.isProcessing,
                    onClick = {
                        showLeaveConfirmation = false

                        // Remove the current assignment through the ViewModel.
                        campaignViewModel.leaveCharacterInCampaign(
                            campaignId = campaignId,
                            onSuccess = {}
                        )
                    }
                ) {
                    Text("Leave Assignment")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showLeaveConfirmation = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun AssignedCharacterCard(
    assignmentName: String,
    character: CharacterEntity?,
    level: Int,
    onLeave: () -> Unit,
    isProcessing: Boolean
) {
    // Display the character currently assigned to the campaign.
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = assignmentName,
                style = MaterialTheme.typography.titleLarge
            )

            // Character details may not be available immediately,
            // so only display the description when the character exists.
            if (character != null) {
                CharacterDescription(character)
            }

            Text(
                text = "Campaign Level: $level",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Disable the button while another assignment operation is running.
            OutlinedButton(
                onClick = onLeave,
                enabled = !isProcessing
            ) {
                Text("Leave Assignment")
            }

            if (isProcessing) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun AvailableCharacterCard(
    character: CharacterEntity,
    isProcessing: Boolean,
    onAssign: () -> Unit
) {
    // Display a saved character that the user can assign to the campaign.
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = character.name,
                style = MaterialTheme.typography.titleLarge
            )

            CharacterDescription(character)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                // Prevent another assignment request while one is processing.
                Button(
                    onClick = onAssign,
                    enabled = !isProcessing
                ) {
                    Text("Assign Character")
                }
            }

            if (isProcessing) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun CharacterDescription(character: CharacterEntity) {
    // Combine the character's species, class and subclass into one description.
    // listOfNotNull skips any fields that have not been provided.
    val description = listOfNotNull(
        character.species,
        character.characterClass,
        character.subclass
    ).joinToString(" • ")

    // Avoid displaying an empty description when all three fields are null or blank.
    if (description.isNotBlank()) {
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}