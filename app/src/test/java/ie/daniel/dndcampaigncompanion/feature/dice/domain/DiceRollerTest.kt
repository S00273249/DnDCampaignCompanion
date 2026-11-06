package ie.daniel.dndcampaigncompanion.feature.dice.domain

import org.junit.Assert
import org.junit.Test

class DiceRollerTest {

    private val diceRoller = DiceRoller()

    @Test
    fun `roll generates correct number of dice`() {
        val configuration = RollConfiguration(
            diceSelections = listOf(
                DiceSelection(DiceType.D6, 2),
                DiceSelection(DiceType.D8, 1)
            )
        )

        val result = diceRoller.roll(configuration)

        Assert.assertEquals(3, result.individualResults.size)
    }

    @Test
    fun `roll generates results within valid dice ranges`() {
        val configuration = RollConfiguration(
            diceSelections = DiceType.entries.map {
                DiceSelection(it, 1)
            }
        )

        val result = diceRoller.roll(configuration)

        result.individualResults.forEach { die ->
            Assert.assertTrue(
                "Invalid result for ${die.diceType}",
                die.value in 1..die.diceType.sides
            )
        }
    }

    @Test
    fun `roll contains correct quantities of each dice type`() {
        val configuration = RollConfiguration(
            diceSelections = listOf(
                DiceSelection(DiceType.D6, 3),
                DiceSelection(DiceType.D20, 2)
            )
        )

        val result = diceRoller.roll(configuration)

        Assert.assertEquals(
            3,
            result.individualResults.count { it.diceType == DiceType.D6 }
        )

        Assert.assertEquals(
            2,
            result.individualResults.count { it.diceType == DiceType.D20 }
        )
    }

    @Test
    fun `roll applies positive modifier`() {
        val configuration = RollConfiguration(
            diceSelections = listOf(
                DiceSelection(DiceType.D6, 2)
            ),
            modifier = 5
        )

        val result = diceRoller.roll(configuration)

        Assert.assertEquals(5, result.modifier)
        Assert.assertEquals(result.subtotal + 5, result.total)
    }

    @Test
    fun `roll applies negative modifier`() {
        val configuration = RollConfiguration(
            diceSelections = listOf(
                DiceSelection(DiceType.D20, 1)
            ),
            modifier = -3
        )

        val result = diceRoller.roll(configuration)

        Assert.assertEquals(-3, result.modifier)
        Assert.assertEquals(result.subtotal - 3, result.total)
    }

    @Test
    fun `roll records its configuration`() {
        val configuration = RollConfiguration(
            diceSelections = listOf(
                DiceSelection(DiceType.D8, 2)
            ),
            modifier = 4
        )

        val result = diceRoller.roll(configuration)

        Assert.assertEquals(configuration, result.configuration)
    }
}