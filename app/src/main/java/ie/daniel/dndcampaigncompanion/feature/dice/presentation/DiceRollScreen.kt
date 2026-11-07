
package ie.daniel.dndcampaigncompanion.feature.dice.presentation

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ie.daniel.dndcampaigncompanion.feature.dice.presentation.rolling.DiceAnimationController
import ie.daniel.dndcampaigncompanion.feature.dice.presentation.rolling.DiceFramingMode
import ie.daniel.dndcampaigncompanion.feature.dice.presentation.rolling.DiceInstanceFactory
import ie.daniel.dndcampaigncompanion.feature.dice.presentation.rolling.DiceRollingBackground
import ie.daniel.dndcampaigncompanion.feature.dice.presentation.rolling.DiceRollingScene
import ie.daniel.dndcampaigncompanion.feature.dice.presentation.rolling.ShakeDetector

@Composable
fun DiceRollScreen(
    viewModel: DiceRollViewModel
) {
    // Observe the ViewModel's state.
    // collectAsState() converts the StateFlow into Compose state,
    // allowing the screen to recompose whenever the state changes.
    val uiState by viewModel.uiState.collectAsState()

    // Stores whether the device has an accelerometer.
    // null means the availability has not been checked yet.
    // Changes to this value trigger recomposition where it is used.
    var hasAccelerometer by remember {
        mutableStateOf<Boolean?>(null)
    }

    // Remember the callbacks so their identities remain stable
    // across recompositions.
    // The ViewModel is the key, so the callback is recreated if
    // the ViewModel instance changes.
    val onShakeDetected = remember(viewModel) {
        { viewModel.onShakeDetected() }
    }

    val onAccelerometerAvailabilityChanged = remember {
        { available: Boolean ->
            // Update the stored sensor availability.
            // This determines whether the manual roll button is shown.
            hasAccelerometer = available
        }
    }

    // Listen for physical shakes only while waiting for a roll.
    // The detector also reports whether an accelerometer is available.
    ShakeDetector(
        enabled = uiState.stage == DiceRollStage.AWAITING_SHAKE,
        onShakeDetected = onShakeDetected,
        onAccelerometerAvailabilityChanged = onAccelerometerAvailabilityChanged
    )

    // Holds the current progress of the dice animation.
    // 0 represents the beginning and 1 represents completion.
    val animationProgress = remember {
        Animatable(1f)
    }

    // Remember the controller so it can be reused across recompositions.
    val animationController = remember {
        DiceAnimationController()
    }

    // Launch the animation when the rolling stage begins
    // and a generated result becomes available.
    // The effect restarts if either key changes.
    LaunchedEffect(uiState.stage, uiState.pendingResult) {
        if (
            uiState.stage == DiceRollStage.ROLLING &&
            uiState.pendingResult != null
        ) {
            // Animate the dice and notify the ViewModel when complete.
            animationController.play(animationProgress) {
                viewModel.completeRollAnimation()
            }
        }
    }

    // Create preview dice using the currently selected quantities.
    // Recalculate the instances only when the quantities change.
    val previewInstances = remember(uiState.diceQuantities) {
        DiceInstanceFactory.createPreviewInstances(
            uiState.diceQuantities
        )
    }

    // Create the actual dice instances from the generated pending result.
    // If no result exists, use an empty list instead.
    val rollingInstances = remember(uiState.pendingResult) {
        uiState.pendingResult?.let {
            DiceInstanceFactory.createInstances(it)
        } ?: emptyList()
    }

    // Use the completed result to create the results showcase.
    // Recalculate the instances only when the latest result changes.
    val resultInstances = remember(uiState.latestResult) {
        uiState.latestResult?.let {
            DiceInstanceFactory.createInstances(it)
        } ?: emptyList()
    }

    // Select which dice instances should appear in the shared scene.
    // Preview dice are shown while waiting, and generated dice while rolling.
    // Other stages do not use this particular scene.
    val sceneInstances = when (uiState.stage) {
        DiceRollStage.AWAITING_SHAKE -> previewInstances
        DiceRollStage.ROLLING -> rollingInstances
        else -> emptyList()
    }

    // Root container allowing the background, scene and controls
    // to be layered on top of one another.
    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        // WAITING / ROLLING

        // Use the same background and 3D scene for both stages.
        if (
            uiState.stage == DiceRollStage.AWAITING_SHAKE ||
            uiState.stage == DiceRollStage.ROLLING
        ) {

            // Display the background behind the transparent 3D scene.
            DiceRollingBackground(
                modifier = Modifier.fillMaxSize()
            )

            // Single shared SceneView for previewing and rolling dice.
            DiceRollingScene(
                instances = sceneInstances,

                // Use the live animation progress while rolling.
                // During the waiting stage, use 1 so preview dice
                // display their calibrated final orientations.
                animationProgress = if (
                    uiState.stage == DiceRollStage.ROLLING
                ) {
                    animationProgress.value
                } else {
                    1f
                },

                modifier = Modifier.fillMaxSize(),
                framingMode = DiceFramingMode.ROLLING
            )

            // Place the instructions and controls at the bottom of the screen.
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(
                        horizontal = 24.dp,
                        vertical = 32.dp
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                // Display the appropriate instruction based on
                // the current stage and accelerometer availability.
                Text(
                    text = if (
                        uiState.stage == DiceRollStage.ROLLING
                    ) {
                        "ROLLING..."
                    } else if (hasAccelerometer == false) {
                        "ROLL DICE"
                    } else {
                        "SHAKE TO ROLL"
                    },
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color(0xFFFFD98A)
                )

                // Show the selected dice quantities and modifier
                // while the user is waiting to initiate the roll.
                if (uiState.stage == DiceRollStage.AWAITING_SHAKE) {

                    Text(
                        text = "${uiState.totalDice} dice  |  Modifier: ${
                            if (uiState.modifier >= 0) "+" else ""
                        }${uiState.modifier}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFFFF0D2)
                    )

                    // Only display the manual roll button when
                    // the device does not have an accelerometer.
                    // It uses the same callback as shake detection,
                    // so both input methods trigger the same ViewModel logic.
                    if (hasAccelerometer == false) {
                        OutlinedButton(
                            onClick = onShakeDetected,
                            colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                                containerColor = Color(0xFF302315),
                                contentColor = Color(0xFFFFD98A)
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                Color(0xFFFFD98A)
                            )
                        ) {
                            Text(
                                text = "Roll Dice",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }

                    // Allow the user to return to dice selection
                    // without starting a roll.
                    OutlinedButton(
                        onClick = {
                            viewModel.returnToSelection()
                        }
                    ) {
                        Text("Back to Selection")
                    }
                }
            }
        }

        // DICE SELECTION

        // Display the dice selection interface when the user
        // is choosing which dice to roll.
        else if (uiState.stage == DiceRollStage.SELECTION) {

            DiceSelectionContent(
                modifier = Modifier
                    .fillMaxSize()
                    // Allow the selection content to scroll if necessary.
                    .verticalScroll(rememberScrollState()),

                // Pass the current selection state to the component.
                diceQuantities = uiState.diceQuantities,
                modifierValue = uiState.modifier,
                totalDice = uiState.totalDice,
                canRoll = uiState.canRoll,

                // Forward dice quantity changes to the ViewModel.
                onDiceQuantityChange = { diceType, quantity ->
                    viewModel.updateDiceQuantity(
                        diceType,
                        quantity
                    )
                },

                // Forward modifier changes to the ViewModel.
                onModifierChange = { modifier ->
                    viewModel.updateModifier(modifier)
                },

                // Confirm the current selection and prepare the roll.
                onConfirmSelection = {
                    viewModel.confirmSelection()
                }
            )
        }

        // RESULTS

        // Display the results interface once the animation is complete.
        else if (uiState.stage == DiceRollStage.RESULTS) {

            DiceResultsContent(
                latestResult = uiState.latestResult,
                history = uiState.rollHistory,

                // Return to selection when the user wants another roll.
                onRollAgain = {
                    viewModel.returnToSelection()
                },

                // Ask the ViewModel to clear the previous roll history.
                onClearHistory = {
                    viewModel.clearHistory()
                },

                // Supply the 3D dice showcase displayed in the results screen.
                diceShowcase = {
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {

                        // Display the background behind the results dice.
                        DiceRollingBackground(
                            modifier = Modifier.fillMaxSize()
                        )

                        // Render the completed dice using the separate
                        // results-screen camera framing configuration.
                        DiceRollingScene(
                            instances = resultInstances,
                            animationProgress = 1f,
                            modifier = Modifier.fillMaxSize(),
                            framingMode = DiceFramingMode.RESULTS
                        )
                    }
                }
            )
        }
    }
}
