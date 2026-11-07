package ie.daniel.dndcampaigncompanion.feature.dice.domain

// An enum class defines a fixed set of available dice types.
// Each type stores its number of sides and the name displayed in the app.
enum class DiceType(
    val sides: Int,
    val displayName: String
) {
    D4(4, "D4"),
    D6(6, "D6"),
    D8(8, "D8"),
    D10(10, "D10"),
    D12(12, "D12"),
    D20(20, "D20"),
    D100(100, "D100")
}