package ie.daniel.dndcampaigncompanion.feature.dice.domain

import java.time.Instant

// Stores the result of one individual die.
data class DieResult(
    val diceType: DiceType,
    val value: Int
)

// Stores the complete result of a die roll.
data class RollResult(
    val configuration: RollConfiguration,
    val individualResults: List<DieResult>,
    val rolledAt: Instant = Instant.now()
) {
    // Adds together the results of all individual dice.
    val subtotal: Int
        get() = individualResults.sumOf { it.value }

    // Gets the modifier from the original roll configuration.
    val modifier: Int
        get() = configuration.modifier

    // Calculates the final total after applying the modifier.
    val total: Int
        get() = subtotal + modifier

    init {
        // Make sure the roll contains at least one result.
        require(individualResults.isNotEmpty()) {
            "A roll must contain at least one result."
        }

        // Check that every die result is within its valid range.
        require(individualResults.all {
            it.value in 1..it.diceType.sides
        }) {
            "Dice results must be within the valid range for their dice type."
        }

        // Make sure the number of results matches the total dice quantity.
        require(
            configuration.diceSelections.sumOf { it.quantity } ==
                    individualResults.size
        ) {
            "The number of results must match the configured dice quantity."
        }

        // Make sure the correct number of results exists for each dice type.
        require(
            configuration.diceSelections.all { selection ->
                individualResults.count {
                    it.diceType == selection.diceType
                } == selection.quantity
            }
        ) {
            "The individual results must match the dice configuration."
        }
    }
}