
package ie.daniel.dndcampaigncompanion.feature.dice.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ie.daniel.dndcampaigncompanion.feature.dice.domain.RollResult

// Shared colours used throughout the results screen.
private val Gold = Color(0xFFFFD98A)
private val GoldMuted = Color(0xFFB88A49)
private val Ivory = Color(0xFFFFF0D2)
private val Ruby = Color(0xFF9D1830)
private val Panel = Color(0xFF211317)
private val PanelLight = Color(0xFF32191F)

/**
 * Displays the completed dice roll, individual results and roll history.
 *
 * The parent screen supplies the dice showcase and the callbacks
 * used to handle user interactions.
 */
@Composable
fun DiceResultsContent(
    latestResult: RollResult?,
    history: List<RollResult>,
    onRollAgain: () -> Unit,
    onClearHistory: () -> Unit,
    diceShowcase: @Composable () -> Unit
) {
    // Arrange the results content vertically.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF120D11))
            // Allow the content to scroll when it exceeds screen height.
            // rememberScrollState() retains the current scroll position.
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 20.dp),
        // Add 20dp of spacing between each direct child of the column.
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        // RESULTS HEADING
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "✦  ROLL COMPLETE  ✦",
                color = Gold,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.sp
            )

            // Decorative horizontal divider with a gold gradient.
            // Transparent edges allow the line to fade into the background.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                GoldMuted,
                                Gold,
                                GoldMuted,
                                Color.Transparent
                            )
                        )
                    )
            )
        }

        // 3D DICE SHOWCASE

        // Reserve a fixed-height area for the 3D dice scene.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(270.dp),
            contentAlignment = Alignment.Center
        ) {
            // Invoke the composable supplied by the parent screen.
            // This keeps the 3D scene implementation separate from this UI.
            diceShowcase()
        }

        // MAIN RESULT PANEL

        // Only display the result panel when a result is available.
        // ?. checks for null, while let provides the non-null result
        // inside the block using the variable name 'result'.
        latestResult?.let { result ->

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(PanelLight, Panel, Color(0xFF180F13))
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                Text(
                    text = "ROLL RESULT",
                    modifier = Modifier.fillMaxWidth(),
                    color = Gold,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    letterSpacing = 2.sp
                )

                HorizontalDivider(color = GoldMuted)

                // Display each individual die result in its own row.
                result.individualResults.forEach { diceResult ->

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Display the dice type, such as D6 or D20.
                        Text(
                            text = diceResult.diceType.displayName,
                            color = Ivory,
                            fontSize = 16.sp
                        )

                        // Display the value generated by that individual die.
                        Text(
                            text = diceResult.value.toString(),
                            color = Gold,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                HorizontalDivider(color = GoldMuted)

                // Display the sum of all individual dice results.
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Subtotal", color = Ivory, fontSize = 15.sp)
                    Text(
                        result.subtotal.toString(),
                        color = Ivory,
                        fontSize = 15.sp
                    )
                }

                // Display the modifier, adding a plus sign for zero
                // and positive values while keeping negative values unchanged.
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Modifier", color = Ivory, fontSize = 15.sp)
                    Text(
                        text = if (result.modifier >= 0) {
                            "+${result.modifier}"
                        } else {
                            result.modifier.toString()
                        },
                        color = Ivory,
                        fontSize = 15.sp
                    )
                }

                HorizontalDivider(color = GoldMuted)

                // Highlight the final total after applying the modifier.
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TOTAL",
                        color = Gold,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )

                    Text(
                        text = result.total.toString(),
                        color = Gold,
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // ROLL AGAIN BUTTON
        Button(
            // Notify the parent screen that the user wants another roll.
            onClick = onRollAgain,
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Ruby,
                contentColor = Gold
            ),
            border = BorderStroke(1.dp, GoldMuted)
        ) {
            Text(
                text = "✦  ROLL AGAIN  ✦",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        // ROLL HISTORY

        // Only show the history section when previous rolls exist.
        if (history.isNotEmpty()) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ROLL HISTORY",
                    color = Gold,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                // Ask the parent screen to clear the saved roll history.
                OutlinedButton(
                    onClick = onClearHistory,
                    border = BorderStroke(1.dp, GoldMuted)
                ) {
                    Text(
                        text = "Clear",
                        color = Gold
                    )
                }
            }

            // Display each previous roll with its position in the history list.
            history.forEachIndexed { index, result ->

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = androidx.compose.material3.CardDefaults.cardColors(
                        containerColor = Panel
                    ),
                    border = BorderStroke(1.dp, GoldMuted)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Calculate the displayed roll number.
                            // For 4 history entries, indices 0 to 3 display
                            // rolls 4, 3, 2 and 1 respectively.
                            Text(
                                text = "ROLL ${history.size - index}",
                                color = Gold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )

                            // Display the final total for this previous roll.
                            Text(
                                text = result.total.toString(),
                                color = Gold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp
                            )
                        }

                        // Convert the individual results into one readable string.
                        // joinToString() places "  +  " between each dice result.
                        // For example: D6: 4  +  D20: 16  +  D8: 5.
                        Text(
                            text = result.individualResults.joinToString("  +  ") {
                                "${it.diceType.displayName}: ${it.value}"
                            },
                            color = Ivory,
                            fontSize = 13.sp
                        )

                        // Display the subtotal and modifier for this previous roll.
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Subtotal: ${result.subtotal}",
                                color = GoldMuted,
                                fontSize = 12.sp
                            )

                            Text(
                                text = "Modifier: ${
                                    if (result.modifier >= 0) "+" else ""
                                }${result.modifier}",
                                color = GoldMuted,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // Add a little extra space after the final history item.
        Spacer(modifier = Modifier.height(12.dp))
    }
}
