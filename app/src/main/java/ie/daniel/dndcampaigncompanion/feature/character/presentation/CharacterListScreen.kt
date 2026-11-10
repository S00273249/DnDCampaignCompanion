package ie.daniel.dndcampaigncompanion.feature.character.presentation

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterListScreen(
    viewModel: CharacterViewModel,
    onCreateCharacter: () -> Unit,
    onCharacterSelected: (String) -> Unit,
    onBack: () -> Unit
) {
    // Observe the character list state from the ViewModel.
    // This provides the characters for the active profile, along with
    // loading and error information.
    val uiState by viewModel.listUiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("My Characters")
                },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Back")
                    }
                }
            )
        },
        // Provide a floating action button for creating a new character.
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateCharacter
            ) {
                Text("+")
            }
        }
    ) { innerPadding ->

        // Display different content depending on the current list state.
        when {
            // Show a loading indicator while the character list is being retrieved.
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            // A profile must be selected before its saved characters can be displayed.
            uiState.activeProfileId == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Select a profile to view its characters.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            // Display any error returned while loading the character list.
            uiState.errorMessage != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.errorMessage
                            ?: "Something went wrong.",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            // Show an empty state when the selected profile has no saved characters.
            uiState.characters.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "No characters yet",
                            style = MaterialTheme.typography.headlineSmall
                        )

                        Text(
                            text = "Create a character to start building your collection.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            else -> {
                // Display the saved characters in a scrollable list.
                // LazyColumn only composes the items needed for the visible list.
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Use each character's ID as a stable key for Compose.
                    items(
                        items = uiState.characters,
                        key = { character -> character.id }
                    ) { character ->

                        // Each character is displayed inside a clickable card.
                        // Selecting the card passes its ID to the navigation callback.
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onCharacterSelected(character.id)
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = character.name,
                                    style = MaterialTheme.typography.titleMedium
                                )

                                // Combine the species and class into a short summary.
                                // Missing values are excluded from the resulting text.
                                val characterDetails = listOfNotNull(
                                    character.species,
                                    character.characterClass
                                ).joinToString(" • ")

                                if (characterDetails.isNotBlank()) {
                                    Text(
                                        text = characterDetails,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                // Display the subclass separately when one has been set.
                                if (!character.subclass.isNullOrBlank()) {
                                    Text(
                                        text = character.subclass,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
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