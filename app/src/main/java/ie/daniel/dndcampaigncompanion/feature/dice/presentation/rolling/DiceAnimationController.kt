
package ie.daniel.dndcampaigncompanion.feature.dice.presentation.rolling

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween

// Controls the animation shared by all dice in a roll.
class DiceAnimationController {

    companion object {
        const val ANIMATION_DURATION_MS = 2200
    }

    /**
     * Resets the animation and animates all dice towards their final
     * positions and rotations.
     *
     * Calls onAnimationComplete once the animation has finished.
     */
    suspend fun play(
        animationProgress: Animatable<Float, AnimationVector1D>,
        onAnimationComplete: () -> Unit
    ) {
        // Start the animation from zero.
        animationProgress.snapTo(0f)

        // Animate all dice to their final state over 2.2 seconds.
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = ANIMATION_DURATION_MS,
                easing = FastOutSlowInEasing
            )
        )

        onAnimationComplete()
    }
}
