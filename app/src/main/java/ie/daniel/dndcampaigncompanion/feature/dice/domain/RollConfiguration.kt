package ie.daniel.dndcampaigncompanion.feature.dice.domain

// Stores all the dice selections and the modifier for a roll.
// As a data class, Kotlin automatically generates useful functions
// like equals(), copy() and toString().
data class RollConfiguration(
    val diceSelections: List<DiceSelection>,
    val modifier: Int = 0
) {
    init {
        // Make sure at least one die type has been selected.
        require(diceSelections.isNotEmpty()) {
            "A roll must contain at least one dice selection."
        }

        // Each dice type should only appear once.
        // Multiple dice of the same type are handled using quantity.
        require(
            diceSelections.map { it.diceType }.distinct().size == diceSelections.size
        ) {
            "Each dice type can only appear once in a roll configuration."
        }
    }
}