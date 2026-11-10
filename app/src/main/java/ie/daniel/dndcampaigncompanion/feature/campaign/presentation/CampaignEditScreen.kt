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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
 * Displays the form for editing an existing campaign.
 *
 * Loads the selected campaign, fills the form with its current information
 * and allows the user to save updated campaign details.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampaignEditScreen(
    campaignId: String,
    campaignViewModel: CampaignViewModel,
    onCampaignUpdated: () -> Unit,
    onBack: () -> Unit
) {
    // Observes the campaign details and the state of the update operation.
    val detailsState by campaignViewModel.detailsUiState.collectAsState()
    val updateState by campaignViewModel.updateUiState.collectAsState()

    val campaign = detailsState.campaign

    // Form values are remembered using the campaign ID as the key.
    // If a different campaign is opened, the form starts with fresh values
    // rather than retaining information from the previously displayed campaign.
    var name by remember(campaignId) { mutableStateOf("") }
    var description by remember(campaignId) { mutableStateOf("") }
    var setting by remember(campaignId) { mutableStateOf("") }
    var location by remember(campaignId) { mutableStateOf("") }
    var maxPlayers by remember(campaignId) { mutableIntStateOf(6) }
    var startingDate by remember(campaignId) { mutableLongStateOf(0L) }

    var showDatePicker by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    // Tracks whether the form has been populated with the campaign's existing data.
    // This prevents the form from being displayed with empty default values
    // before the campaign information has loaded.
    var formInitialised by remember(campaignId) {
        mutableStateOf(false)
    }

    // Requests the selected campaign's details when the campaign ID changes.
    LaunchedEffect(campaignId) {
        campaignViewModel.loadCampaignDetails(campaignId)
    }

    // Runs when the campaign data becomes available.
    // The existing values are copied into the form only once for this campaign,
    // allowing the user to edit the fields without them being overwritten
    // during subsequent recompositions.
    LaunchedEffect(campaign) {
        if (campaign != null && !formInitialised) {
            name = campaign.name
            description = campaign.description.orEmpty()
            setting = campaign.setting.orEmpty()
            location = campaign.location.orEmpty()
            maxPlayers = campaign.maxPlayers
            startingDate = campaign.startingDate
            formInitialised = true
        }
    }

    // Formats the selected starting date for display.
    val dateFormatter = remember {
        DateTimeFormatter.ofPattern("dd MMMM yyyy")
    }

    // Convert the stored timestamp into a readable date.
    // UTC prevents the displayed date from shifting because of the device's time zone.
    val formattedDate = if (startingDate > 0L) {
        Instant.ofEpochMilli(startingDate)
            .atZone(ZoneOffset.UTC)
            .toLocalDate()
            .format(dateFormatter)
    } else {
        "Select starting date"
    }

    val isSaving = updateState.isUpdating

    // Wait until the campaign has loaded and the form has been populated.
    if (detailsState.isLoading || !formInitialised) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text("Loading campaign details...")
        }
        return
    }

    // If the campaign could not be loaded, display an error and
    // provide an option to return to the previous screen.
    if (campaign == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = detailsState.errorMessage
                    ?: "Campaign could not be found."
            )

            TextButton(onClick = onBack) {
                Text("Back")
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Edit Campaign",
            style = androidx.compose.material3.MaterialTheme.typography.headlineMedium
        )

        // Campaign name is required. Changing it clears any previous
        // validation message so the user can correct the input.
        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
                validationError = null
            },
            label = { Text("Campaign name *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !isSaving
        )

        // Optional campaign description.
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description (optional)") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            enabled = !isSaving
        )

        // Optional setting or world information.
        OutlinedTextField(
            value = setting,
            onValueChange = { setting = it },
            label = { Text("Setting / World (optional)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !isSaving
        )

        // Optional campaign location.
        OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            label = { Text("Location (optional)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !isSaving
        )

        Text(
            text = "Starting date *",
            style = androidx.compose.material3.MaterialTheme.typography.titleMedium
        )

        // Displays the current starting date or the newly selected date.
        // The field is read-only because the date is changed through the date picker.
        OutlinedTextField(
            value = formattedDate,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSaving
        )

        // Opens the date picker so the campaign's starting date can be changed.
        TextButton(
            onClick = { showDatePicker = true },
            enabled = !isSaving
        ) {
            Text("Choose date")
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Allows the maximum player count to be adjusted between 1 and 12.
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
            enabled = !isSaving
        )

        // Labels indicate the minimum and maximum slider values.
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

        // Displays errors returned by the ViewModel during the update operation.
        updateState.errorMessage?.let {
            Text(
                text = it,
                color = androidx.compose.material3.MaterialTheme.colorScheme.error
            )
        }

        Button(
            onClick = {
                // Remove leading and trailing spaces before validating the name.
                val trimmedName = name.trim()

                // Check that the required campaign information has been provided.
                when {
                    trimmedName.isEmpty() -> {
                        validationError = "Please enter a campaign name."
                    }

                    startingDate <= 0L -> {
                        validationError = "Please select a starting date."
                    }

                    else -> {
                        validationError = null

                        // Pass the updated values and campaign ID to the ViewModel.
                        // The success callback returns to the previous screen
                        // once the update has completed successfully.
                        campaignViewModel.updateCampaign(
                            campaignId = campaignId,
                            name = trimmedName,
                            description = description,
                            setting = setting,
                            location = location,
                            startingDate = startingDate,
                            maxPlayers = maxPlayers,
                            onSuccess = onCampaignUpdated
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSaving
        ) {
            // Changes the button text while the update operation is running.
            Text(
                if (isSaving) {
                    "Saving changes..."
                } else {
                    "Save Changes"
                }
            )
        }

        // Returns to the previous screen without saving the changes.
        TextButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSaving
        ) {
            Text("Cancel")
        }
    }

    // Only displays the date picker when requested by the user.
    if (showDatePicker) {
        // Uses the existing starting date as the initial selection when available.
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
                        // Update the form with the newly selected date.
                        // The safe call means nothing changes if no date was selected.
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
