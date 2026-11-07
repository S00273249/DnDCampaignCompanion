package ie.daniel.dndcampaigncompanion.feature.dice.presentation

import ie.daniel.dndcampaigncompanion.feature.dice.domain.DiceType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DiceRollViewModelTest {

    @Test
    fun `starts in selection stage`() {
        val viewModel = DiceRollViewModel()

        assertEquals(
            DiceRollStage.SELECTION,
            viewModel.uiState.value.stage
        )
    }

    @Test
    fun `confirming selection moves to awaiting shake`() {
        val viewModel = DiceRollViewModel()

        viewModel.confirmSelection()

        assertEquals(
            DiceRollStage.AWAITING_SHAKE,
            viewModel.uiState.value.stage
        )
    }

    @Test
    fun `shake triggers rolling stage and generates result`() {
        val viewModel = DiceRollViewModel()

        viewModel.confirmSelection()
        viewModel.onShakeDetected()

        assertEquals(
            DiceRollStage.ROLLING,
            viewModel.uiState.value.stage
        )

        assertNotNull(viewModel.uiState.value.pendingResult)
    }

    @Test
    fun `completing animation moves to results and saves history`() {
        val viewModel = DiceRollViewModel()

        viewModel.confirmSelection()
        viewModel.onShakeDetected()
        viewModel.completeRollAnimation()

        val state = viewModel.uiState.value

        assertEquals(DiceRollStage.RESULTS, state.stage)
        assertNotNull(state.latestResult)
        assertNull(state.pendingResult)
        assertEquals(1, state.rollHistory.size)
    }

    @Test
    fun `shake is ignored before selection is confirmed`() {
        val viewModel = DiceRollViewModel()

        viewModel.onShakeDetected()

        assertEquals(
            DiceRollStage.SELECTION,
            viewModel.uiState.value.stage
        )

        assertNull(viewModel.uiState.value.pendingResult)
    }

    @Test
    fun `returning from results restores selection stage`() {
        val viewModel = DiceRollViewModel()

        viewModel.confirmSelection()
        viewModel.onShakeDetected()
        viewModel.completeRollAnimation()
        viewModel.returnToSelection()

        assertEquals(
            DiceRollStage.SELECTION,
            viewModel.uiState.value.stage
        )
    }

    @Test
    fun `dice quantities can be updated`() {
        val viewModel = DiceRollViewModel()

        viewModel.updateDiceQuantity(DiceType.D6, 3)

        assertEquals(
            3,
            viewModel.uiState.value.diceQuantities[DiceType.D6]
        )
    }

    @Test
    fun `duplicate shake does not generate another roll`() {
        val viewModel = DiceRollViewModel()

        viewModel.confirmSelection()
        viewModel.onShakeDetected()

        val firstResult = viewModel.uiState.value.pendingResult

        viewModel.onShakeDetected()

        assertEquals(
            firstResult,
            viewModel.uiState.value.pendingResult
        )
    }
}