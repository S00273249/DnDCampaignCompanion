package ie.daniel.dndcampaigncompanion.feature.dice.domain

data class RollConfiguration(
    val diceSelections: List<DiceSelection>,
    val modifier: Int = 0
) {
    init {
        require(diceSelections.isNotEmpty()) {
            "A roll must contain at least one dice selection."
        }

        require(diceSelections.map { it.diceType }.distinct().size == diceSelections.size) {
            "Each dice type can only appear once in a roll configuration."
        }
    }
}