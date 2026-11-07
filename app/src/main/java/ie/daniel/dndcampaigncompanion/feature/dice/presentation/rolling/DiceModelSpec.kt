
package ie.daniel.dndcampaigncompanion.feature.dice.presentation.rolling

import ie.daniel.dndcampaigncompanion.feature.dice.domain.DiceType

// Stores the model file and display scale for a particular dice type.
data class DiceModelSpec(
    val diceType: DiceType,
    val assetPath: String,
    val scaleToUnits: Float = 1.0f
)

// Stores the model specifications for all available dice types.
object DiceModelSpecs {

    private val specifications = listOf(
        DiceModelSpec(
            diceType = DiceType.D4,
            assetPath = "models/ruby_d4.glb"
        ),
        DiceModelSpec(
            diceType = DiceType.D6,
            assetPath = "models/ruby_d6.glb"
        ),
        DiceModelSpec(
            diceType = DiceType.D8,
            assetPath = "models/ruby_d8.glb"
        ),
        DiceModelSpec(
            diceType = DiceType.D10,
            assetPath = "models/ruby_d10.glb"
        ),
        DiceModelSpec(
            diceType = DiceType.D12,
            assetPath = "models/ruby_d12.glb"
        ),
        DiceModelSpec(
            diceType = DiceType.D20,
            assetPath = "models/ruby_d20.glb"
        ),
        DiceModelSpec(
            diceType = DiceType.D100,
            assetPath = "models/ruby_d100.glb"
        )
    )

    // Finds and returns the model specification for the requested dice type.
    // first() checks each entry and returns the first one that matches.
    fun get(diceType: DiceType): DiceModelSpec {
        return specifications.first { it.diceType == diceType }
    }

    // Returns the complete list of available dice model specifications.
    fun getAll(): List<DiceModelSpec> {
        return specifications
    }
}
