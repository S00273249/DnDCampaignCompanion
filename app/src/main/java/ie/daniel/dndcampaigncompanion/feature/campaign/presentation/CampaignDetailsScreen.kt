package ie.daniel.dndcampaigncompanion.feature.campaign.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ie.daniel.dndcampaigncompanion.data.local.entity.CampaignRole
import ie.daniel.dndcampaigncompanion.data.local.entity.CampaignStatus
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * Displays the details of a selected campaign, including its settings,
 * members and any characters assigned to those members.
 *
 * The available management options depend on the active profile's
 * membership role and whether the campaign is active.
 */
@Composable
fun CampaignDetailsScreen(
    campaignViewModel: CampaignViewModel,
    campaignId: String,
    onBack: () -> Unit,
    onEditCampaign: (String) -> Unit,
    onManageMembers: (String) -> Unit,
    onManageCharacter: (String) -> Unit
) {
    // Observes the campaign details and the main campaign state separately.
    val detailsState by campaignViewModel.detailsUiState.collectAsState()
    val campaignListState by campaignViewModel.uiState.collectAsState()

    // Loads the selected campaign whenever the campaign ID changes.
    LaunchedEffect(campaignId) {
        campaignViewModel.loadCampaignDetails(campaignId)
    }

    val campaign = detailsState.campaign
    val activeProfileId = campaignListState.activeProfileId

    // Finds the membership belonging to the currently selected profile.
    // This is used to determine which campaign actions the user can access.
    val currentMembership = detailsState.members.firstOrNull {
        it.membership.userId == activeProfileId
    }

    // Only an active campaign's Dungeon Master can edit campaign details
    // or manage its members.
    val canEditCampaign =
        campaign != null &&
                campaign.status == CampaignStatus.ACTIVE &&
                currentMembership?.membership?.role == CampaignRole.DM

    // Only an active campaign's players can manage their assigned character.
    val canManageCharacter =
        campaign != null &&
                campaign.status == CampaignStatus.ACTIVE &&
                currentMembership?.membership?.role == CampaignRole.PLAYER

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Returns to the campaign list.
        Button(
            onClick = onBack
        ) {
            Text("Back to Campaigns")
        }

        // Displays different content depending on the current loading state.
        when {
            detailsState.isLoading -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                    Text("Loading campaign...")
                }
            }

            detailsState.errorMessage != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    detailsState.errorMessage?.let { errorMessage ->
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            campaign != null -> {
                // Convert the stored timestamp into a readable date.
                // UTC ensures the date is not shifted by the device's time zone.
                val dateFormatter = DateTimeFormatter.ofPattern(
                    "dd MMMM yyyy"
                )

                val formattedDate = Instant
                    .ofEpochMilli(campaign.startingDate)
                    .atZone(ZoneOffset.UTC)
                    .toLocalDate()
                    .format(dateFormatter)

                // Counts only members with the PLAYER role.
                // Dungeon Masters are not included in the player limit.
                val activePlayerCount = detailsState.members.count {
                    it.membership.role == CampaignRole.PLAYER
                }

                // A LazyColumn allows the campaign information and member list
                // to scroll when the content is larger than the available space.
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Campaign name, status, description and management actions.
                    item {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = campaign.name,
                                style = MaterialTheme.typography.headlineMedium
                            )

                            Text(
                                text = "Status: ${campaign.status.name}",
                                style = MaterialTheme.typography.titleMedium
                            )

                            campaign.description?.let {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }

                            // These buttons are only displayed to the DM
                            // when the campaign is active.
                            if (canEditCampaign) {
                                Button(
                                    onClick = {
                                        onEditCampaign(campaign.id)
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Edit Campaign")
                                }

                                Button(
                                    onClick = {
                                        onManageMembers(campaign.id)
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Manage Members")
                                }
                            }

                            // Players are given access to their own character
                            // management screen while the campaign is active.
                            if (canManageCharacter) {
                                Button(
                                    onClick = {
                                        onManageCharacter(campaign.id)
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Manage My Character")
                                }
                            }
                        }
                    }

                    // Displays the campaign's general information.
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "Campaign Information",
                                    style = MaterialTheme.typography.titleLarge
                                )

                                // Optional fields display a fallback when no value
                                // has been saved for them.
                                DetailRow(
                                    label = "Setting",
                                    value = campaign.setting ?: "Not specified"
                                )

                                DetailRow(
                                    label = "Location",
                                    value = campaign.location ?: "Not specified"
                                )

                                DetailRow(
                                    label = "Starting Date",
                                    value = formattedDate
                                )

                                DetailRow(
                                    label = "Maximum Players",
                                    value = campaign.maxPlayers.toString()
                                )
                            }
                        }
                    }

                    // Displays the Dungeon Master and players currently
                    // associated with the campaign.
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "Members",
                                    style = MaterialTheme.typography.titleLarge
                                )

                                Text(
                                    text = "$activePlayerCount / ${campaign.maxPlayers} players",
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                HorizontalDivider()

                                // Display all members with the Dungeon Master role.
                                detailsState.members
                                    .filter {
                                        it.membership.role == CampaignRole.DM
                                    }
                                    .forEach { member ->
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = "Dungeon Master",
                                                style = MaterialTheme.typography.titleMedium
                                            )

                                            // Profile information may be missing,
                                            // so a fallback username is provided.
                                            Text(
                                                text = member.profile?.username
                                                    ?: "Unknown profile",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    }

                                // Display players and their assigned characters.
                                // forEachIndexed provides a numbered label for each player.
                                detailsState.members
                                    .filter {
                                        it.membership.role == CampaignRole.PLAYER
                                    }
                                    .forEachIndexed { index, member ->
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = "Player ${index + 1}",
                                                style = MaterialTheme.typography.titleMedium
                                            )

                                            Text(
                                                text = member.profile?.username
                                                    ?: "Unknown profile",
                                                style = MaterialTheme.typography.bodyMedium
                                            )

                                            val character = member.character

                                            if (character != null) {
                                                // Display the name of the character
                                                // assigned to this campaign member.
                                                Text(
                                                    text = "Character: ${character.name}",
                                                    style = MaterialTheme.typography.bodyMedium
                                                )

                                                // Build a short character description using
                                                // whichever of these fields are available.
                                                // joinToString separates them with a bullet.
                                                val characterDescription = listOfNotNull(
                                                    character.species,
                                                    character.characterClass,
                                                    character.subclass
                                                ).joinToString(" • ")

                                                if (characterDescription.isNotBlank()) {
                                                    Text(
                                                        text = characterDescription,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            } else {
                                                // Shown when the player has not assigned
                                                // a character to this campaign.
                                                Text(
                                                    text = "No character assigned",
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
    }
}

/**
 * Displays a label and its corresponding value in a horizontal row.
 *
 * Used to keep the campaign information section consistent
 * without repeating the same layout for every field.
 */
@Composable
private fun DetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
