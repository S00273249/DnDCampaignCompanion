package ie.daniel.dndcampaigncompanion.feature.dice.presentation.rolling

import ie.daniel.dndcampaigncompanion.feature.dice.domain.DiceRoller
import ie.daniel.dndcampaigncompanion.feature.dice.domain.DiceSelection
import ie.daniel.dndcampaigncompanion.feature.dice.domain.DiceType
import ie.daniel.dndcampaigncompanion.feature.dice.domain.RollConfiguration
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DiceInstanceFactoryTest {

    private val diceRoller = DiceRoller(Random(42))

    @Test
    fun `creates one instance for every individual die`() {
        val configuration = RollConfiguration(
            diceSelections = listOf(
                DiceSelection(DiceType.D6, 2),
                DiceSelection(DiceType.D20, 1)
            )
        )

        val result = diceRoller.roll(configuration)

        val instances = DiceInstanceFactory.createInstances(result)

        assertEquals(3, instances.size)
    }

    @Test
    fun `preserves the dice types and individual results`() {
        val configuration = RollConfiguration(
            diceSelections = listOf(
                DiceSelection(DiceType.D6, 2),
                DiceSelection(DiceType.D20, 1)
            )
        )

        val result = diceRoller.roll(configuration)

        val instances = DiceInstanceFactory.createInstances(result)

        assertEquals(
            result.individualResults.map { it.diceType },
            instances.map { it.diceType }
        )

        assertEquals(
            result.individualResults.map { it.value },
            instances.map { it.result }
        )
    }

    @Test
    fun `assigns a unique id to every die`() {
        val configuration = RollConfiguration(
            diceSelections = listOf(
                DiceSelection(DiceType.D8, 4)
            )
        )

        val result = diceRoller.roll(configuration)

        val instances = DiceInstanceFactory.createInstances(result)

        assertEquals(
            listOf(0, 1, 2, 3),
            instances.map { it.id }
        )
    }

    @Test
    fun `centres a single die at the origin`() {
        val configuration = RollConfiguration(
            diceSelections = listOf(
                DiceSelection(DiceType.D20, 1)
            )
        )

        val result = diceRoller.roll(configuration)

        val instance = DiceInstanceFactory.createInstances(result).single()

        assertEquals(0f, instance.position.x, 0.001f)
        assertEquals(0f, instance.position.y, 0.001f)
        assertEquals(0f, instance.position.z, 0.001f)
    }

    @Test
    fun `assigns separate positions to multiple dice`() {
        val configuration = RollConfiguration(
            diceSelections = listOf(
                DiceSelection(DiceType.D6, 3)
            )
        )

        val result = diceRoller.roll(configuration)

        val instances = DiceInstanceFactory.createInstances(result)

        assertEquals(3, instances.size)

        assertTrue(
            instances.map { it.position.x }.distinct().size > 1
        )

        assertTrue(
            instances.map { it.position.y }.distinct().size > 1
        )
    }
}