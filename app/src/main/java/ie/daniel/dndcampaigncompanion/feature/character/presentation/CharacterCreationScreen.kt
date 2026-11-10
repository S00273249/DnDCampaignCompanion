package ie.daniel.dndcampaigncompanion.feature.character.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterCreationScreen(
    viewModel: CharacterViewModel,
    onCharacterCreated: () -> Unit,
    onBack: () -> Unit
) {
    // Observe the ViewModel state to track saving progress and any errors.
    val operationState by viewModel.operationUiState.collectAsState()

    // rememberSaveable keeps form values available across configuration changes
    // and when the composable is recreated by the saved-state mechanism.
    var name by rememberSaveable { mutableStateOf("") }
    var species by rememberSaveable { mutableStateOf("") }
    var characterClass by rememberSaveable { mutableStateOf("") }
    var subclass by rememberSaveable { mutableStateOf("") }
    var background by rememberSaveable { mutableStateOf("") }
    var alignment by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var backstory by rememberSaveable { mutableStateOf("") }

    // Ability scores are initially stored as strings because text fields
    // accept text input. They are converted to integers when the form is submitted.
    var strength by rememberSaveable { mutableStateOf("") }
    var dexterity by rememberSaveable { mutableStateOf("") }
    var constitution by rememberSaveable { mutableStateOf("") }
    var intelligence by rememberSaveable { mutableStateOf("") }
    var wisdom by rememberSaveable { mutableStateOf("") }
    var charisma by rememberSaveable { mutableStateOf("") }

    // Local validation errors are separate from errors returned by the ViewModel.
    var validationError by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Create Character")
                },
                navigationIcon = {
                    // Prevent leaving the screen while a character is being saved.
                    TextButton(
                        onClick = onBack,
                        enabled = !operationState.isProcessing
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    ) { innerPadding ->

        // The form contains many fields, so verticalScroll allows the user
        // to move through the entire form on smaller screens.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            Text(
                text = "Character Information",
                style = MaterialTheme.typography.titleLarge
            )

            // Character name is the only required text field.
            // Changing it also clears any previous name validation error.
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    validationError = null
                },
                label = { Text("Character Name *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !operationState.isProcessing
            )

            OutlinedTextField(
                value = species,
                onValueChange = { species = it },
                label = { Text("Species") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !operationState.isProcessing
            )

            OutlinedTextField(
                value = characterClass,
                onValueChange = { characterClass = it },
                label = { Text("Class") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !operationState.isProcessing
            )

            OutlinedTextField(
                value = subclass,
                onValueChange = { subclass = it },
                label = { Text("Subclass") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !operationState.isProcessing
            )

            OutlinedTextField(
                value = background,
                onValueChange = { background = it },
                label = { Text("Background") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !operationState.isProcessing
            )

            OutlinedTextField(
                value = alignment,
                onValueChange = { alignment = it },
                label = { Text("Alignment") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !operationState.isProcessing
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                enabled = !operationState.isProcessing
            )

            OutlinedTextField(
                value = backstory,
                onValueChange = { backstory = it },
                label = { Text("Backstory") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
                enabled = !operationState.isProcessing
            )

            Text(
                text = "Ability Scores",
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = "Enter the character's base ability scores. These can be left blank and filled in later.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Each ability score uses the same reusable input component.
            AbilityScoreField(
                label = "Strength",
                value = strength,
                onValueChange = { strength = it },
                enabled = !operationState.isProcessing
            )

            AbilityScoreField(
                label = "Dexterity",
                value = dexterity,
                onValueChange = { dexterity = it },
                enabled = !operationState.isProcessing
            )

            AbilityScoreField(
                label = "Constitution",
                value = constitution,
                onValueChange = { constitution = it },
                enabled = !operationState.isProcessing
            )

            AbilityScoreField(
                label = "Intelligence",
                value = intelligence,
                onValueChange = { intelligence = it },
                enabled = !operationState.isProcessing
            )

            AbilityScoreField(
                label = "Wisdom",
                value = wisdom,
                onValueChange = { wisdom = it },
                enabled = !operationState.isProcessing
            )

            AbilityScoreField(
                label = "Charisma",
                value = charisma,
                onValueChange = { charisma = it },
                enabled = !operationState.isProcessing
            )

            // Display validation errors produced locally by the form.
            validationError?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Display errors returned by the character creation operation.
            operationState.errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Button(
                onClick = {
                    // Check the required character name before attempting to save.
                    if (name.isBlank()) {
                        validationError = "Please enter a character name."
                        return@Button
                    }

                    // Group the six ability score inputs so they can all be checked
                    // using the same validation condition.
                    val scoreInputs = listOf(
                        strength,
                        dexterity,
                        constitution,
                        intelligence,
                        wisdom,
                        charisma
                    )

                    // Blank scores are allowed, but any entered value must
                    // be convertible to a whole number.
                    if (scoreInputs.any {
                            it.isNotBlank() && it.toIntOrNull() == null
                        }
                    ) {
                        validationError = "Ability scores must be valid whole numbers."
                        return@Button
                    }

                    // Pass the validated form data to the ViewModel.
                    // Optional text fields are trimmed and converted to null
                    // when left blank. Ability scores are also converted to
                    // integers, with blank values becoming null.
                    viewModel.createCharacter(
                        name = name.trim(),
                        species = species.trim().ifBlank { null },
                        characterClass = characterClass.trim().ifBlank { null },
                        subclass = subclass.trim().ifBlank { null },
                        background = background.trim().ifBlank { null },
                        alignment = alignment.trim().ifBlank { null },
                        description = description.trim().ifBlank { null },
                        backstory = backstory.trim().ifBlank { null },
                        strength = strength.toIntOrNull(),
                        dexterity = dexterity.toIntOrNull(),
                        constitution = constitution.toIntOrNull(),
                        intelligence = intelligence.toIntOrNull(),
                        wisdom = wisdom.toIntOrNull(),
                        charisma = charisma.toIntOrNull(),
                        onSuccess = {
                            // Notify the parent navigation when saving succeeds.
                            onCharacterCreated()
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !operationState.isProcessing
            ) {
                // Replace the button text with a progress indicator while saving.
                if (operationState.isProcessing) {
                    CircularProgressIndicator()
                } else {
                    Text("Create Character")
                }
            }
        }
    }
}

@Composable
private fun AbilityScoreField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean
) {
    // Reusable input for an individual ability score.
    // Only digit characters are accepted, keeping the input suitable
    // for conversion to an integer when the character is created.
    OutlinedTextField(
        value = value,
        onValueChange = { input ->
            if (input.all { it.isDigit() }) {
                onValueChange(input)
            }
        },
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        enabled = enabled
    )
}