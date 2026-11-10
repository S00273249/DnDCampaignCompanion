package ie.daniel.dndcampaigncompanion.feature.character.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterDetailsScreen(
    viewModel: CharacterViewModel,
    characterId: String,
    onEditCharacter: (String) -> Unit,
    onCharacterDeleted: () -> Unit,
    onBack: () -> Unit
) {
    // Observe the character details and any ongoing character operations.
    val detailState by viewModel.detailUiState.collectAsState()
    val operationState by viewModel.operationUiState.collectAsState()

    // Controls whether the user is currently being asked to confirm deletion.
    var showDeleteConfirmation by remember {
        mutableStateOf(false)
    }

    // Load the selected character whenever the character ID changes.
    LaunchedEffect(characterId) {
        viewModel.loadCharacter(characterId)
    }

    // Ask the user to confirm before deleting the character.
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = {
                showDeleteConfirmation = false
            },
            title = {
                Text("Delete Character?")
            },
            text = {
                Text(
                    "This character will be removed from your character list. " +
                            "Existing campaign history will be retained."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Request deletion through the ViewModel.
                        // Only close the dialog and navigate away after success.
                        viewModel.deleteCharacter(
                            characterId = characterId,
                            onSuccess = {
                                showDeleteConfirmation = false
                                onCharacterDeleted()
                            }
                        )
                    },
                    enabled = !operationState.isProcessing
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Character Details")
                },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Back")
                    }
                }
            )
        }
    ) { innerPadding ->

        // Display different content depending on whether the character
        // is loading, failed to load or has been successfully retrieved.
        when {
            detailState.isLoading -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            detailState.errorMessage != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = detailState.errorMessage
                            ?: "Unable to load character.",
                        color = MaterialTheme.colorScheme.error
                    )

                    // Return to the previous screen if the character cannot be loaded.
                    Button(
                        onClick = onBack,
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Text("Go Back")
                    }
                }
            }

            detailState.character != null -> {
                val character = detailState.character!!

                // The character has been loaded successfully.
                // A scrollable column allows all details to be viewed on smaller screens.
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = character.name,
                        style = MaterialTheme.typography.headlineMedium
                    )

                    // Build a short summary from the character's species and class.
                    // Missing values are excluded from the summary.
                    val characterSummary = listOfNotNull(
                        character.species,
                        character.characterClass
                    ).joinToString(" • ")

                    if (characterSummary.isNotBlank()) {
                        Text(
                            text = characterSummary,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Divider()

                    Text(
                        text = "Character Information",
                        style = MaterialTheme.typography.titleLarge
                    )

                    // Display each optional character detail only when it has a value.
                    CharacterDetailItem("Species", character.species)
                    CharacterDetailItem("Class", character.characterClass)
                    CharacterDetailItem("Subclass", character.subclass)
                    CharacterDetailItem("Background", character.background)
                    CharacterDetailItem("Alignment", character.alignment)

                    // Description and backstory are separate sections because
                    // they can contain longer pieces of text.
                    if (!character.description.isNullOrBlank()) {
                        Divider()

                        Text(
                            text = "Description",
                            style = MaterialTheme.typography.titleLarge
                        )

                        Text(
                            text = character.description,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    if (!character.backstory.isNullOrBlank()) {
                        Divider()

                        Text(
                            text = "Backstory",
                            style = MaterialTheme.typography.titleLarge
                        )

                        Text(
                            text = character.backstory,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    Divider()

                    Text(
                        text = "Ability Scores",
                        style = MaterialTheme.typography.titleLarge
                    )

                    // Display all six ability scores.
                    // AbilityScoreRow handles scores that have not yet been entered.
                    AbilityScoreRow(
                        label = "Strength",
                        score = character.strength
                    )

                    AbilityScoreRow(
                        label = "Dexterity",
                        score = character.dexterity
                    )

                    AbilityScoreRow(
                        label = "Constitution",
                        score = character.constitution
                    )

                    AbilityScoreRow(
                        label = "Intelligence",
                        score = character.intelligence
                    )

                    AbilityScoreRow(
                        label = "Wisdom",
                        score = character.wisdom
                    )

                    AbilityScoreRow(
                        label = "Charisma",
                        score = character.charisma
                    )

                    Divider()

                    // Display errors returned by character operations,
                    // such as a failed deletion.
                    operationState.errorMessage?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    // Pass the character ID to the edit navigation callback
                    // so the edit screen knows which character to load.
                    Button(
                        onClick = {
                            onEditCharacter(character.id)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !operationState.isProcessing
                    ) {
                        Text("Edit Character")
                    }

                    // Open the confirmation dialog rather than deleting immediately.
                    OutlinedButton(
                        onClick = {
                            showDeleteConfirmation = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !operationState.isProcessing
                    ) {
                        Text("Delete Character")
                    }

                    // Show a progress indicator while a character operation is running.
                    if (operationState.isProcessing) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CharacterDetailItem(
    label: String,
    value: String?
) {
    // Skip optional character details that are null or blank.
    if (!value.isNullOrBlank()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun AbilityScoreRow(
    label: String,
    score: Int?
) {
    // Display the ability name on the left and its value on the right.
    // If the score has not been entered, show "Not set" instead.
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge
        )

        Text(
            text = score?.toString() ?: "Not set",
            style = MaterialTheme.typography.titleMedium
        )
    }
}