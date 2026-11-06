package ie.daniel.dndcampaigncompanion.feature.dice.domain

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