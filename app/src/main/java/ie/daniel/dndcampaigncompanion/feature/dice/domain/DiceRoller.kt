package ie.daniel.dndcampaigncompanion.feature.dice.domain

import kotlin.random.Random

class DiceRoller(
    private val random: Random = Random.Default
) {

    fun roll(configuration: RollConfiguration): RollResult {

        val results = configuration.diceSelections.flatMap { selection ->

            List(selection.quantity) {
                DieResult(
                    diceType = selection.diceType,
                    value = random.nextInt(
                        from = 1,
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