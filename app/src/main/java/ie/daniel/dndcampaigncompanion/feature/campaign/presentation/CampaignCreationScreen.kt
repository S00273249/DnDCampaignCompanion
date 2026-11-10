package ie.daniel.dndcampaigncompanion.feature.campaign.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * Displays the form used to create a new campaign.
 *
 * Collects the campaign details, validates the required fields and
 * passes the information to the CampaignViewModel for creation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampaignCreationScreen(
    campaignViewModel: CampaignViewModel,
    onCampaignCreated: () -> Unit,
    onBack: () -> Unit
) {
    // Remember the form values so they are retained when Compose recomposes.
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var setting by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var maxPlayers by remember { mutableIntStateOf(6) }
    var startingDate by remember { mutableLongStateOf(0L) }
    var showDatePicker by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    // Observes the creation state from the ViewModel, including loading
    // and error information, so the screen can respond to creation progress.
    val creationState by campaignViewModel.creationUiState.collectAsState()

    // Used to display the selected date in a readable format.
    val dateFormatter = remember {
        DateTimeFormatter.ofPattern("dd MMMM yyyy")
    }

    // DatePicker returns the selected date as milliseconds since the Unix epoch.
    // Convert it to a LocalDate and format it for display.
    // UTC is used so the selected date is not shifted by the device's time zone.
    val formattedDate = if (startingDate > 0L) {
        Instant.ofEpochMilli(startingDate)
            .atZone(ZoneOffset.UTC)
            .toLocalDate()
            .format(dateFormatter)
    } else {
        "Select starting date"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Create Campaign",
            style = androidx.compose.material3.MaterialTheme.typography.headlineMedium
        )

        // Campaign name is the only text field that must be completed.
        // Changing it also clears any previous validation message.
        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
                validationError = null
            },
            label = { Text("Campaign name *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !creationState.isCreating
        )

        // Optional campaign description.
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description (optional)") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            enabled = !creationState.isCreating
        )

        // Optional setting or world information for the campaign.
        OutlinedTextField(
            value = setting,
            onValueChange = { setting = it },
            label = { Text("Setting / World (optional)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !creationState.isCreating
        )

        // Optional location where the campaign takes place.
        OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            label = { Text("Location (optional)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !creationState.isCreating
        )

        Text(
            text = "Starting date *",
            style = androidx.compose.material3.MaterialTheme.typography.titleMedium
        )

        // Read-only field displays the formatted date selected by the user.
        OutlinedTextField(
            value = formattedDate,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            enabled = !creationState.isCreating
        )

        // Opens the Material 3 date picker.
        TextButton(
            onClick = { showDatePicker = true },
            enabled = !creationState.isCreating
        ) {
            Text("Choose date")
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Allows the user to choose between 1 and 12 players.
        // The slider value is a Float, so it is converted to an Int
        // and restricted to the allowed range.
        Text(
            text = "Maximum players: $maxPlayers",
            style = androidx.compose.material3.MaterialTheme.typography.titleMedium
        )

        Slider(
            value = maxPlayers.toFloat(),
            onValueChange = {
                maxPlayers = it.toInt().coerceIn(1, 12)
            },
            valueRange = 1f..12f,
            steps = 10,
            enabled = !creationState.isCreating
        )

        // Labels show the minimum and maximum values of the slider.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("1 player")
            Text("12 players")
        }

        // Displays validation errors produced by this screen.
        validationError?.let {
            Text(
                text = it,
                color = androidx.compose.material3.MaterialTheme.colorScheme.error
            )
        }

        // Displays errors returned by the ViewModel during campaign creation.
        creationState.errorMessage?.let {
            Text(
                text = it,
                color = androidx.compose.material3.MaterialTheme.colorScheme.error
            )
        }

        Button(
            onClick = {
                // Remove leading and trailing spaces before validating the name.
                val trimmedName = name.trim()

                // Check that all required information has been provided.
                when {
                    trimmedName.isEmpty() -> {
                        validationError = "Please enter a campaign name."
                    }

                    startingDate <= 0L -> {
                        validationError = "Please select a starting date."
                    }

                    else -> {
                        validationError = null

                        // Pass the form values to the ViewModel.
                        // The success callback is called once the campaign
                        // has been created successfully.
                        campaignViewModel.createCampaign(
                            name = trimmedName,
                            description = description,
                            setting = setting,
                            location = location,
                            startingDate = startingDate,
                            maxPlayers = maxPlayers,
                            onSuccess = {
                                onCampaignCreated()
                            }
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !creationState.isCreating
        ) {
            // Changes the button text while the creation operation is running.
            Text(
                if (creationState.isCreating) {
                    "Creating campaign..."
                } else {
                    "Create Campaign"
                }
            )
        }

        // Returns to the previous screen without creating a campaign.
        TextButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(),
            enabled = !creationState.isCreating
        ) {
            Text("Cancel")
        }
    }

    // Only displays the date picker when the user requests it.
    if (showDatePicker) {
        // If a date has already been selected, use it as the initial selection.
        // Otherwise, the date picker starts without a selected date.
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = if (startingDate > 0L) {
                startingDate
            } else {
                null
            }
        )

        DatePickerDialog(
            onDismissRequest = {
                showDatePicker = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Update the starting date if the user has selected one.
                        // The safe call prevents updating the value when nothing
                        // has been selected.
                        datePickerState.selectedDateMillis?.let {
                            startingDate = it
                            validationError = null
                        }

                        showDatePicker = false
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDatePicker = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                showModeToggle = false
            )
        }
    }
}
