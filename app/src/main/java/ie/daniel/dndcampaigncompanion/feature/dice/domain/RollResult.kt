package ie.daniel.dndcampaigncompanion.feature.dice.domain

import java.time.Instant

data class DieResult(
    val diceType: DiceType,
    val value: Int
)

data class RollResult(
    val configuration: RollConfiguration,
    val individualResults: List<DieResult>,
    val rolledAt: Instant = Instant.now()
) {
    val subtotal: Int
        get() = individualResults.sumOf { it.value }

    val modifier: Int
        get() = configuration.modifier

    val total: Int
        get() = subtotal + modifier

    init {
        require(individualResults.isNotEmpty()) {
            "A roll must contain at least one result."
        }

        require(individualResults.all { it.value in 1..it.diceType.sides }) {
            "Dice results must be within the valid range for their dice type."
        }

        require(
            configuration.diceSelections.sumOf { it.quantity } ==
                    individualResults.size
        ) {
            "The number of results must match the configured dice quantity."
        }

        require(
            configuration.diceSelections.all { selection ->
                individualResults.count { it.diceType == selection.diceType } ==
                        selection.quantity
            }
        ) {
            "The individual results must match the dice configuration."
        }
    }
}