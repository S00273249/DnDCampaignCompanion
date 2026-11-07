package ie.daniel.dndcampaigncompanion.feature.dice.domain

import kotlin.random.Random

/**
 * Generates random results for a dice roll.
 *
 * A Random instance can be passed in to produce predictable
 * results when running tests.
 */
class DiceRoller(
    private val random: Random = Random.Default
) {

    /**
     * Rolls the selected dice and returns the individual results.
     * Each die gets a random value between 1 and its maximum sides.
     */
    fun roll(configuration: RollConfiguration): RollResult {

        // flatMap creates a list of results for each dice selection
        // and combines them all into one list.
        val results = configuration.diceSelections.flatMap { selection ->
            List(selection.quantity) {
                DieResult(
                    diceType = selection.diceType,
                    value = random.nextInt(
                        from = 1,
                        // nextInt doesn't include the upper limit,
                        // so we add 1 to allow the highest dice value.
                        until = selection.diceType.sides + 1
                    )
                )
            }
        }

        return RollResult(
            configuration = configuration,
            individualResults = results
        )
    }
}