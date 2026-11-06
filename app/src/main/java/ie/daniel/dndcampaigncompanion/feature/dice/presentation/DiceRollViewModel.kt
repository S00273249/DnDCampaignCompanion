
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

data class DiceRollUiState(
    val diceQuantities: Map<DiceType, Int> = mapOf(
        DiceType.D20 to 1
    ),
    val modifier: Int = 0,
    val latestResult: RollResult? = null,
    val rollHistory: List<RollResult> = emptyList()
) {
    val totalDice: Int
        get() = diceQuantities.values.sum()

    val canRoll: Boolean
        get() = totalDice > 0
}

class DiceRollViewModel(
    private val diceRoller: DiceRoller = DiceRoller()
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiceRollUiState())
    val uiState: StateFlow<DiceRollUiState> = _uiState.asStateFlow()

    fun updateDiceQuantity(
        diceType: DiceType,
        quantity: Int
    ) {
        require(quantity >= 0) {
            "Dice quantity cannot be negative."
        }

        _uiState.update { currentState ->

            val updatedQuantities = currentState.diceQuantities.toMutableMap()

            if (quantity == 0) {
                updatedQuantities.remove(diceType)
            } else {
                updatedQuantities[diceType] = quantity
            }

            currentState.copy(
                diceQuantities = updatedQuantities
            )
        }
    }

    fun updateModifier(modifier: Int) {
        _uiState.update { currentState ->
            currentState.copy(modifier = modifier)
        }
    }

    fun rollDice() {
        val currentState = _uiState.value

        if (!currentState.canRoll) {
            return
        }

        val configuration = RollConfiguration(
            diceSelections = currentState.diceQuantities.map { (type, quantity) ->
                DiceSelection(
                    diceType = type,
                    quantity = quantity
                )
            },
            modifier = currentState.modifier
        )

        val result = diceRoller.roll(configuration)

        _uiState.update { state ->
            state.copy(
                latestResult = result,
                rollHistory = listOf(result) + state.rollHistory
            )
        }
    }

    fun clearHistory() {
        _uiState.update { currentState ->
            currentState.copy(
                rollHistory = emptyList()
            )
        }
    }

    fun resetConfiguration() {
        _uiState.update { currentState ->
            currentState.copy(
                diceQuantities = emptyMap(),
                modifier = 0
            )
        }
    }
}
