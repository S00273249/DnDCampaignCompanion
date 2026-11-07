
package ie.daniel.dndcampaigncompanion.feature.dice.presentation

import androidx.lifecycle.ViewModel
import ie.daniel.dndcampaigncompanion.feature.dice.domain.DiceRoller
import ie.daniel.dndcampaigncompanion.feature.dice.domain.DiceSelection
import ie.daniel.dndcampaigncompanion.feature.dice.domain.DiceType
import ie.daniel.dndcampaigncompanion.feature.dice.domain.RollConfiguration
import ie.daniel.dndcampaigncompanion.feature.dice.domain.RollResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// Represents the different stages of the dice-rolling process.
enum class DiceRollStage {
    SELECTION,
    AWAITING_SHAKE,
    ROLLING,
    RESULTS
}

// Stores all the information required by the dice roller UI.
// A new instance is created whenever the state changes.
data class DiceRollUiState(
    val diceQuantities: Map<DiceType, Int> = emptyMap(),
    val modifier: Int = 0,
    val stage: DiceRollStage = DiceRollStage.SELECTION,
    val pendingResult: RollResult? = null,
    val latestResult: RollResult? = null,
    val rollHistory: List<RollResult> = emptyList()
) {
    // Calculate the total number of selected dice.
    // values retrieves the quantities from the map, and sum() adds them.
    // For example, quantities of 3 D6s and 2 D20s produce a total of 5.
    val totalDice: Int
        get() = diceQuantities.values.sum()

    // A roll is only possible when at least one die has been selected.
    val canRoll: Boolean
        get() = totalDice > 0
}

// Manages the dice roller's state and coordinates the rolling process.
// Extending ViewModel allows this state to survive screen recompositions.
class DiceRollViewModel(
    // Allow a DiceRoller to be supplied for testing.
    // If none is provided, create the default implementation.
    private val diceRoller: DiceRoller = DiceRoller()
) : ViewModel() {

    // Mutable state is kept private so only the ViewModel can update it.
    private val _uiState = MutableStateFlow(DiceRollUiState())

    // Expose a read-only version of the state for the UI to observe.
    // asStateFlow() prevents the UI from directly modifying the state.
    val uiState: StateFlow<DiceRollUiState> = _uiState.asStateFlow()

    // Update the quantity of a particular dice type.
    fun updateDiceQuantity(
        diceType: DiceType,
        quantity: Int
    ) {
        // Reject negative quantities.
        require(quantity >= 0) {
            "Dice quantity cannot be negative."
        }

        // update() creates a new state based on the current state.
        _uiState.update { currentState ->

            // Create a mutable copy so entries can be added, changed or removed.
            // The original map in the current state is left unchanged.
            val updatedQuantities =
                currentState.diceQuantities.toMutableMap()

            if (quantity == 0) {
                // Remove the dice type when its quantity reaches zero.
                updatedQuantities.remove(diceType)
            } else {
                // Add or update the quantity for this dice type.
                updatedQuantities[diceType] = quantity
            }

            // Create a new UI state containing the updated dice quantities.
            // All other properties retain their existing values.
            currentState.copy(
                diceQuantities = updatedQuantities
            )
        }
    }

    // Update the modifier applied to the dice roll.
    fun updateModifier(modifier: Int) {
        _uiState.update { currentState ->
            // Create a new state with the updated modifier.
            currentState.copy(modifier = modifier)
        }
    }

    /**
     * Confirms the selected dice and moves to the
     * screen where the user is prompted to roll.
     */
    fun confirmSelection() {
        _uiState.update { currentState ->

            // Only allow confirmation when the user is selecting dice
            // and at least one die has been selected.
            if (
                currentState.stage == DiceRollStage.SELECTION &&
                currentState.canRoll
            ) {
                currentState.copy(
                    stage = DiceRollStage.AWAITING_SHAKE,
                    // Clear any previous pending result before a new roll.
                    pendingResult = null
                )
            } else {
                // Leave the state unchanged if confirmation is not allowed.
                currentState
            }
        }
    }

    /**
     * Called when a valid shake is detected or the fallback
     * Roll Dice button is pressed.
     *
     * Generates the actual random results and starts rolling.
     */
    fun onShakeDetected() {
        // Retrieve the current state before generating a result.
        val currentState = _uiState.value

        // Only allow a roll to start while waiting for a shake.
        // This prevents additional triggers during other stages.
        if (currentState.stage != DiceRollStage.AWAITING_SHAKE) {
            return
        }

        // Do not generate a result if no dice have been selected.
        if (!currentState.canRoll) {
            return
        }

        // Convert the selected dice map into a list of DiceSelection objects.
        // map() transforms each map entry into a new object.
        // (type, quantity) destructures each entry into its key and value.
        //
        // For example:
        // D6 -> 3 and D20 -> 2 become:
        // [DiceSelection(D6, 3), DiceSelection(D20, 2)].
        val configuration = RollConfiguration(
            diceSelections = currentState.diceQuantities.map { (type, quantity) ->
                DiceSelection(
                    diceType = type,
                    quantity = quantity
                )
            },
            modifier = currentState.modifier
        )

        // Generate the random dice values using the domain-layer DiceRoller.
        val result = diceRoller.roll(configuration)

        // Store the generated result and move into the rolling stage.
        _uiState.update { state ->

            // Check the stage again before updating.
            // If the state has already changed, leave it untouched.
            if (state.stage == DiceRollStage.AWAITING_SHAKE) {
                state.copy(
                    stage = DiceRollStage.ROLLING,
                    pendingResult = result
                )
            } else {
                state
            }
        }
    }

    /**
     * Called when the 3D rolling animation and
     * settling pause have finished.
     */
    fun completeRollAnimation() {
        _uiState.update { currentState ->

            // Retrieve the result that was generated before the animation.
            val result = currentState.pendingResult

            // Only complete the roll if the animation stage is active
            // and a pending result is available.
            if (
                currentState.stage == DiceRollStage.ROLLING &&
                result != null
            ) {
                currentState.copy(
                    stage = DiceRollStage.RESULTS,

                    // The pending result is no longer needed once completed.
                    pendingResult = null,

                    // Store the completed result for the results screen.
                    latestResult = result,

                    // Add the newest result at the beginning of the history.
                    rollHistory = listOf(result) + currentState.rollHistory
                )
            } else {
                // Leave the state unchanged if the roll cannot be completed.
                currentState
            }
        }
    }

    /**
     * Returns to dice selection for another roll.
     * Previous results and history are preserved.
     */
    fun returnToSelection() {
        _uiState.update { currentState ->

            // Only allow returning from the results or waiting stages.
            if (
                currentState.stage == DiceRollStage.RESULTS ||
                currentState.stage == DiceRollStage.AWAITING_SHAKE
            ) {
                currentState.copy(
                    stage = DiceRollStage.SELECTION,
                    // Clear any pending result when leaving the waiting stage.
                    pendingResult = null
                )
            } else {
                // Do not change the state during an active roll.
                currentState
            }
        }
    }

    // Remove all previous rolls from the history.
    fun clearHistory() {
        _uiState.update { currentState ->
            // Replace the existing history with an empty list.
            // Other state properties, including latestResult, are preserved.
            currentState.copy(
                rollHistory = emptyList()
            )
        }
    }

    // Reset the selected dice and modifier to their default values.
    fun resetConfiguration() {
        _uiState.update { currentState ->
            // Remove all selected dice and reset the modifier to zero.
            // The current stage, results and history are preserved.
            currentState.copy(
                diceQuantities = emptyMap(),
                modifier = 0
            )
        }
    }
}
