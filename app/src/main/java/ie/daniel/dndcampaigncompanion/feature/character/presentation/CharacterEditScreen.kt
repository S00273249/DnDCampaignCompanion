package ie.daniel.dndcampaigncompanion.feature.character.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.LaunchedEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterEditScreen(
    viewModel: CharacterViewModel,
    characterId: String,
    onCharacterUpdated: () -> Unit,
    onBack: () -> Unit
) {
    // Observe the character details and the state of any ongoing operations.
    val detailState by viewModel.detailUiState.collectAsState()
    val operationState by viewModel.operationUiState.collectAsState()

    // The character being edited, once it has been loaded.
    val character = detailState.character

    // Store the form values separately from the database character.
    // Using characterId as the rememberSaveable key ensures the form state
    // is associated with the correct character if the ID changes.
    var name by rememberSaveable(characterId) { mutableStateOf("") }
    var species by rememberSaveable(characterId) { mutableStateOf("") }
    var characterClass by rememberSaveable(characterId) { mutableStateOf("") }
    var subclass by rememberSaveable(characterId) { mutableStateOf("") }
    var background by rememberSaveable(characterId) { mutableStateOf("") }
    var alignment by rememberSaveable(characterId) { mutableStateOf("") }
    var description by rememberSaveable(characterId) { mutableStateOf("") }
    var backstory by rememberSaveable(characterId) { mutableStateOf("") }

    // Ability scores are stored as strings while the user edits them
    // and converted to integers when the changes are saved.
    var strength by rememberSaveable(characterId) { mutableStateOf("") }
    var dexterity by rememberSaveable(characterId) { mutableStateOf("") }
    var constitution by rememberSaveable(characterId) { mutableStateOf("") }
    var intelligence by rememberSaveable(characterId) { mutableStateOf("") }
    var wisdom by rememberSaveable(characterId) { mutableStateOf("") }
    var charisma by rememberSaveable(characterId) { mutableStateOf("") }

    // Tracks whether the saved character information has been copied
    // into the form so that it is not overwritten during recomposition.
    var hasLoadedCharacter by remember(characterId) {
        mutableStateOf(false)
    }

    // Stores validation errors produced by the form itself.
    var validationError by rememberSaveable(characterId) {
        mutableStateOf<String?>(null)
    }

    // Request the selected character from the ViewModel when the screen opens
    // or when a different character ID is supplied.
    LaunchedEffect(characterId) {
        viewModel.loadCharacter(characterId)
    }

    // Populate the form after the matching character has been loaded.
    // The ID check prevents details from a previously selected character
    // from being copied into the current form.
    LaunchedEffect(character?.id) {
        if (character != null && character.id == characterId && !hasLoadedCharacter) {
            name = character.name
            species = character.species.orEmpty()
            characterClass = character.characterClass.orEmpty()
            subclass = character.subclass.orEmpty()
            background = character.background.orEmpty()
            alignment = character.alignment.orEmpty()
            description = character.description.orEmpty()
            backstory = character.backstory.orEmpty()

            // Convert nullable integer scores into strings for the text fields.
            // Missing scores are represented by empty strings.
            strength = character.strength?.toString().orEmpty()
            dexterity = character.dexterity?.toString().orEmpty()
            constitution = character.constitution?.toString().orEmpty()
            intelligence = character.intelligence?.toString().orEmpty()
            wisdom = character.wisdom?.toString().orEmpty()
            charisma = character.charisma?.toString().orEmpty()

            hasLoadedCharacter = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Edit Character")
                },
                navigationIcon = {
                    // Prevent leaving the screen while changes are being saved.
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

        // Do not display the form until the character has loaded
        // and its saved values have been copied into the form fields.
        when {
            detailState.isLoading || !hasLoadedCharacter -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                }
            }

            // Display an error if the character could not be found or loaded.
            character == null || detailState.errorMessage != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = detailState.errorMessage
                            ?: "Character not found.",
                        color = MaterialTheme.colorScheme.error
                    )

                    Button(onClick = onBack) {
                        Text("Go Back")
                    }
                }
            }

            else -> {
                // Display the editable form once the character data is ready.
                // The scrollable column allows the user to access every field.
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

                    // Character name is required. Editing it clears a previous
                    // name validation error.
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
                        text = "Update the character's base ability scores.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Reuse the same input component for each of the six scores.
                    EditAbilityScoreField(
                        label = "Strength",
                        value = strength,
                        onValueChange = { strength = it },
                        enabled = !operationState.isProcessing
                    )

                    EditAbilityScoreField(
                        label = "Dexterity",
                        value = dexterity,
                        onValueChange = { dexterity = it },
                        enabled = !operationState.isProcessing
                    )

                    EditAbilityScoreField(
                        label = "Constitution",
                        value = constitution,
                        onValueChange = { constitution = it },
                        enabled = !operationState.isProcessing
                    )

                    EditAbilityScoreField(
                        label = "Intelligence",
                        value = intelligence,
                        onValueChange = { intelligence = it },
                        enabled = !operationState.isProcessing
                    )

                    EditAbilityScoreField(
                        label = "Wisdom",
                        value = wisdom,
                        onValueChange = { wisdom = it },
                        enabled = !operationState.isProcessing
                    )

                    EditAbilityScoreField(
                        label = "Charisma",
                        value = charisma,
                        onValueChange = { charisma = it },
                        enabled = !operationState.isProcessing
                    )

                    // Show validation errors from the form separately
                    // from errors returned by the ViewModel.
                    validationError?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    operationState.errorMessage?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    Button(
                        onClick = {
                            // Check that the required character name has been entered.
                            if (name.isBlank()) {
                                validationError = "Please enter a character name."
                                return@Button
                            }

                            // Check all six ability score fields.
                            // Blank scores are allowed, but entered values must
                            // be valid whole numbers.
                            val scoreInputs = listOf(
                                strength,
                                dexterity,
                                constitution,
                                intelligence,
                                wisdom,
                                charisma
                            )

                            if (scoreInputs.any {
                                    it.isNotBlank() && it.toIntOrNull() == null
                                }
                            ) {
                                validationError =
                                    "Ability scores must be valid whole numbers."
                                return@Button
                            }

                            // Create an updated copy of the existing character.
                            // This preserves fields not being edited here, such as
                            // its ID and ownership information.
                            viewModel.updateCharacter(
                                character = character.copy(
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
                                    charisma = charisma.toIntOrNull()
                                ),
                                onSuccess = onCharacterUpdated
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !operationState.isProcessing
                    ) {
                        // Show progress while the updated character is being saved.
                        if (operationState.isProcessing) {
                            CircularProgressIndicator()
                        } else {
                            Text("Save Changes")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EditAbilityScoreField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean
) {
    // Reusable ability score input.
    // Only digit characters are accepted so the value can be converted
    // into an integer when the updated character is saved.
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