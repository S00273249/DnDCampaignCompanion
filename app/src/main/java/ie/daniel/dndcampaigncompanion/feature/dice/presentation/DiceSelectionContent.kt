
package ie.daniel.dndcampaigncompanion.feature.dice.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
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
import ie.daniel.dndcampaigncompanion.feature.dice.domain.DiceType

// Shared colours used throughout the dice selection interface.
private val Gold = Color(0xFFFFD98A)
private val GoldMuted = Color(0xFFB88A49)
private val Ruby = Color(0xFF9D1830)
private val RubyBright = Color(0xFFCC3045)
private val Panel = Color(0xFF211317)
private val PanelLight = Color(0xFF32191F)
private val Ivory = Color(0xFFFFF0D2)

/**
 * Displays the dice selection interface.
 *
 * The current selection and modifier are supplied by the parent,
 * while user actions are passed back through callback functions.
 */
@Composable
fun DiceSelectionContent(
    modifier: Modifier = Modifier,
    diceQuantities: Map<DiceType, Int>,
    modifierValue: Int,
    totalDice: Int,
    canRoll: Boolean,
    onDiceQuantityChange: (DiceType, Int) -> Unit,
    onModifierChange: (Int) -> Unit,
    onConfirmSelection: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF171014),
                        Color(0xFF26191B),
                        Color(0xFF120D11)
                    )
                )
            )
            .padding(horizontal = 20.dp, vertical = 18.dp),
        // Adds 20dp of vertical spacing between the main sections.
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        // Header
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "❖",
                    color = RubyBright,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.size(12.dp))

                Text(
                    text = "Dice Roller",
                    color = Gold,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
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

        // Dice type heading
        Text(
            text = "Select Dice",
            color = Gold,
            fontSize = 23.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        // Split the seven dice types into rows containing up to four.
        // This produces one row of four and a second row of three.
        DiceType.entries.chunked(4).forEach { diceRow ->

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                diceRow.forEach { diceType ->

                    // Retrieve the current quantity for this dice type.
                    // If it isn't in the map, its quantity defaults to zero.
                    val quantity = diceQuantities[diceType] ?: 0

                    DiceTypeTile(
                        diceType = diceType,
                        selected = quantity > 0,
                        modifier = Modifier
                            // Give each tile an equal share of the row's width.
                            .weight(1f)
                            .aspectRatio(0.88f),
                        onClick = {
                            // Increase the quantity when the user taps a tile.
                            onDiceQuantityChange(
                                diceType,
                                quantity + 1
                            )
                        }
                    )
                }

                // Add empty spaces to the final row so its tiles
                // have the same widths and alignment as the first row.
                repeat(4 - diceRow.size) {
                    Spacer(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(0.88f)
                    )
                }
            }
        }

        // Selected dice
        Text(
            text = "Selected Dice",
            color = Gold,
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Panel,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, GoldMuted.copy(alpha = 0.8f))
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Keep only dice types that currently have a quantity above zero.
                val selectedDice = DiceType.entries.filter {
                    (diceQuantities[it] ?: 0) > 0
                }

                if (selectedDice.isEmpty()) {
                    // Display a message when no dice have been selected.
                    Text(
                        text = "Tap a die above to add it",
                        color = Ivory.copy(alpha = 0.65f),
                        fontSize = 14.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp),
                        textAlign = TextAlign.Center
                    )
                } else {
                    // Display one row for each selected dice type.
                    selectedDice.forEach { diceType ->

                        val quantity = diceQuantities[diceType] ?: 0

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Display the symbol associated with this dice type.
                            Text(
                                text = diceSymbol(diceType),
                                color = Gold,
                                fontSize = 25.sp,
                                modifier = Modifier.size(38.dp),
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = "${diceType.displayName}  × $quantity",
                                color = Ivory,
                                fontSize = 17.sp,
                                modifier = Modifier.weight(1f)
                            )

                            // Decrease the quantity by one.
                            // The button is disabled when the quantity is zero.
                            QuantityButton(
                                label = "−",
                                enabled = quantity > 0,
                                onClick = {
                                    onDiceQuantityChange(
                                        diceType,
                                        quantity - 1
                                    )
                                }
                            )

                            Spacer(Modifier.size(8.dp))

                            // Increase the quantity by one.
                            QuantityButton(
                                label = "+",
                                onClick = {
                                    onDiceQuantityChange(
                                        diceType,
                                        quantity + 1
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }

        // Modifier
        Text(
            text = "Modifier",
            color = Gold,
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Panel,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, GoldMuted)
        ) {
            Row(
                modifier = Modifier.padding(
                    horizontal = 18.dp,
                    vertical = 12.dp
                ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Decrease the modifier by one.
                QuantityButton(
                    label = "−",
                    onClick = {
                        onModifierChange(modifierValue - 1)
                    }
                )

                // Display a plus sign for positive modifiers.
                // Negative values already include their minus sign,
                // while zero is displayed without an additional sign.
                Text(
                    text = if (modifierValue > 0) {
                        "+$modifierValue"
                    } else {
                        modifierValue.toString()
                    },
                    color = Gold,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold
                )

                // Increase the modifier by one.
                QuantityButton(
                    label = "+",
                    onClick = {
                        onModifierChange(modifierValue + 1)
                    }
                )
            }
        }

        // Display the total number of individual dice selected.
        Text(
            text = "Total dice: $totalDice",
            color = Ivory.copy(alpha = 0.8f),
            fontSize = 14.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        // Main action
        Button(
            // Ask the parent to confirm the current selection.
            onClick = onConfirmSelection,
            // Prevent confirmation when no dice have been selected.
            enabled = canRoll,
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Ruby,
                contentColor = Gold,
                disabledContainerColor = PanelLight,
                disabledContentColor = GoldMuted
            ),
            border = BorderStroke(1.5.dp, Gold)
        ) {
            Text(
                text = "♧  READY TO ROLL",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
    }
}

/**
 * Displays an individual selectable dice tile.
 *
 * Its appearance changes depending on whether that dice type
 * has already been selected.
 */
@Composable
private fun DiceTypeTile(
    diceType: DiceType,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    // Choose a different gradient depending on the selection state.
    val background = if (selected) {
        Brush.verticalGradient(
            listOf(
                Color(0xFF5B2028),
                Color(0xFF291318)
            )
        )
    } else {
        Brush.verticalGradient(
            listOf(
                Color(0xFF351B20),
                Color(0xFF201317)
            )
        )
    }

    Column(
        modifier = modifier
            .background(background, RoundedCornerShape(12.dp))
            .border(
                // Selected dice have a thicker gold border.
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) Gold else GoldMuted,
                shape = RoundedCornerShape(12.dp)
            )
            // Make the entire tile respond to taps.
            .clickable(onClick = onClick)
            .padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = diceSymbol(diceType),
            color = Gold,
            fontSize = 35.sp,
            fontWeight = FontWeight.Light
        )

        Spacer(Modifier.height(5.dp))

        Text(
            text = diceType.displayName,
            color = Gold,
            fontSize = 17.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Reusable button for increasing or decreasing a value.
 *
 * The enabled parameter controls whether the button can be tapped.
 */
@Composable
private fun QuantityButton(
    label: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(width = 38.dp, height = 34.dp)
            // Ignore taps when the button is disabled.
            .clickable(enabled = enabled, onClick = onClick),
        color = if (enabled) Ruby else PanelLight,
        shape = RoundedCornerShape(9.dp),
        border = BorderStroke(
            1.dp,
            GoldMuted.copy(alpha = if (enabled) 0.8f else 0.3f)
        )
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                color = if (enabled) Gold else GoldMuted,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// Returns the symbol associated with each dice type.
private fun diceSymbol(diceType: DiceType): String =
    when (diceType) {
        DiceType.D4 -> "△"
        DiceType.D6 -> "◇"
        DiceType.D8 -> "✧"
        DiceType.D10 -> "◈"
        DiceType.D12 -> "⬡"
        DiceType.D20 -> "✥"
        DiceType.D100 -> "⑩"
    }
