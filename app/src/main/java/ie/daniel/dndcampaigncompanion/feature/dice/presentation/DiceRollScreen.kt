
package ie.daniel.dndcampaigncompanion.feature.dice.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ie.daniel.dndcampaigncompanion.feature.dice.domain.DiceType

@Composable
fun DiceRollScreen(
    viewModel: DiceRollViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = "Dice Roller",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Choose your dice",
            style = MaterialTheme.typography.titleMedium
        )

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DiceType.entries.forEach { diceType ->

                    val quantity = uiState.diceQuantities[diceType] ?: 0

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = diceType.displayName,
                            style = MaterialTheme.typography.titleMedium
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.updateDiceQuantity(
                                        diceType,
                                        (quantity - 1).coerceAtLeast(0)
                                    )
                                },
                                enabled = quantity > 0
                            ) {
                                Text("-")
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = quantity.toString(),
                                style = MaterialTheme.typography.titleMedium
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            OutlinedButton(
                                onClick = {
                                    viewModel.updateDiceQuantity(
                                        diceType,
                                        quantity + 1
                                    )
                                }
                            ) {
                                Text("+")
                            }
                        }
                    }
                }
            }
        }

        Text(
            text = "Modifier",
            style = MaterialTheme.typography.titleMedium
        )

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = {
                        viewModel.updateModifier(uiState.modifier - 1)
                    }
                ) {
                    Text("-")
                }

                Text(
                    text = if (uiState.modifier > 0) {
                        "+${uiState.modifier}"
                    } else {
                        uiState.modifier.toString()
                    },
                    style = MaterialTheme.typography.headlineSmall
                )

                OutlinedButton(
                    onClick = {
                        viewModel.updateModifier(uiState.modifier + 1)
                    }
                ) {
                    Text("+")
                }
            }
        }

        Text(
            text = "Total dice: ${uiState.totalDice}",
            style = MaterialTheme.typography.bodyMedium
        )

        Button(
            onClick = { viewModel.rollDice() },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState.canRoll
        ) {
            Text("Roll Dice")
        }

        OutlinedButton(
            onClick = { viewModel.resetConfiguration() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Reset Configuration")
        }

        uiState.latestResult?.let { result ->

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Latest Roll",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Text(
                        text = result.individualResults
                            .joinToString(" + ") {
                                "${it.diceType.displayName}: ${it.value}"
                            },
                        style = MaterialTheme.typography.bodyLarge
                    )

                    HorizontalDivider()

                    Text(
                        text = "Subtotal: ${result.subtotal}",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Text(
                        text = "Modifier: ${if (result.modifier >= 0) "+" else ""}${result.modifier}",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Text(
                        text = "Total: ${result.total}",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            }
        }

        if (uiState.rollHistory.isNotEmpty()) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Roll History",
                    style = MaterialTheme.typography.titleLarge
                )

                OutlinedButton(
                    onClick = { viewModel.clearHistory() }
                ) {
                    Text("Clear")
                }
            }

            uiState.rollHistory.forEachIndexed { index, result ->

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Roll ${uiState.rollHistory.size - index}",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(
                            text = result.individualResults.joinToString(" + ") {
                                "${it.diceType.displayName}: ${it.value}"
                            }
                        )

                        Text(
                            text = "Total: ${result.total}",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}
