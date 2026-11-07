package ie.daniel.dndcampaigncompanion.feature.dice.domain

// A data class is used to store the dice type and quantity together.
// Kotlin automatically generates functions like equals(), copy()
// and toString(), so we don't have to write them ourselves.
data class DiceSelection(
    val diceType: DiceType,
    val quantity: Int
) {
    init {
        require(quantity > 0) {
            "Dice quantity must be greater than zero."
        }
    }
}