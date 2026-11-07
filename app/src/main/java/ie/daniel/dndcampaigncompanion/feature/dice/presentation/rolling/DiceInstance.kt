
package ie.daniel.dndcampaigncompanion.feature.dice.presentation.rolling

import ie.daniel.dndcampaigncompanion.feature.dice.domain.DiceType

// Represents one individual die in a 3D roll.
// Each die has its own result, ID and position,
// even when multiple dice have the same type.
data class DiceInstance(
    val id: Int,
    val diceType: DiceType,
    val result: Int,
    val position: DiceScenePosition
)

// Stores the position of a die within the 3D scene.
// X controls horizontal position, Y controls vertical position
// and Z controls depth. All coordinates default to the origin.
data class DiceScenePosition(
    val x: Float = 0f,
    val y: Float = 0f,
    val z: Float = 0f
)
