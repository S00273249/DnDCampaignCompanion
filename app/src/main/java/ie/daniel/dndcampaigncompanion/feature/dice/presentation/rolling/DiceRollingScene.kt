
package ie.daniel.dndcampaigncompanion.feature.dice.presentation.rolling

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import io.github.sceneview.*
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.node.ModelNode
import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt

// Determines which screen is displaying the dice.
// Each mode uses its own camera framing settings.
enum class DiceFramingMode {
    ROLLING,
    RESULTS
}

/**
 * Shared 3D scene used to render all dice involved in a roll.
 *
 * Camera framing adapts to the dimensions of the dice arrangement.
 * Rolling and results screens have independent framing configurations.
 */
@Composable
fun DiceRollingScene(
    instances: List<DiceInstance>,
    animationProgress: Float,
    modifier: Modifier = Modifier,
    framingMode: DiceFramingMode = DiceFramingMode.ROLLING
) {
    // Create or retrieve the shared rendering engine and model loader.
    // remember keeps these resources across Compose recompositions.
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)

    // Position the camera along the Z axis.
    val cameraNode = rememberCameraNode(engine) {
        position = Position(z = 4.5f)
    }

    // Set up the main light used to illuminate the dice models.
    val mainLightNode = rememberMainLightNode(engine) {
        intensity = 100_000.0f
    }

    // Keep animation progress within the expected range of 0 to 1.
    // Values below 0 become 0, and values above 1 become 1.
    val progress = animationProgress.coerceIn(0f, 1f)

    // Calculate the dimensions of the dice grid.
    val totalDice = instances.size

    // Use the square root of the dice count to estimate a balanced
    // number of columns. ceil() rounds upwards to ensure enough space.
    // For example, 10 dice gives ceil(sqrt(10)) = 4 columns.
    // Use 1 column when there are no dice to avoid division by zero.
    val columns = if (totalDice == 0) {
        1
    } else {
        ceil(sqrt(totalDice.toDouble())).toInt()
    }

    // Divide the total dice count by the number of columns.
    // ceil() rounds upwards so a partially filled final row is included.
    // For example, 10 dice across 4 columns requires 3 rows.
    val rows = if (totalDice == 0) {
        1
    } else {
        ceil(totalDice.toDouble() / columns).toInt()
    }

    // Use whichever grid dimension is larger.
    // This provides one value for adjusting the camera framing.
    val gridDimension = max(columns, rows)

    // Each screen has its own independently adjustable framing.
    val dynamicFramingPadding = when (framingMode) {

        DiceFramingMode.ROLLING -> {
            // Existing rolling-screen framing.
            // Preserved exactly as previously calibrated.
            when (totalDice) {
                0, 1 -> 2.0f
                2, 3, 4 -> 3f
                5, 6, 7, 8, 9 -> 4.0f

                // For larger grids, increase padding based on the
                // largest dimension. Each additional grid unit adds 0.7.
                else -> 2.0f + (gridDimension - 1) * 0.7f
            }
        }

        DiceFramingMode.RESULTS -> {
            // Initial results-screen framing.
            // Lower padding brings the dice closer.
            when (totalDice) {
                0, 1 -> 1.3f
                2, 3, 4 -> 1.2f
                5, 6, 7, 8, 9 -> 1.1f
                13, 14 -> 1.1f
                15, 16 -> 1.2f

                // Reduce padding as the grid grows.
                // max() ensures the calculated value never falls below 0.75.
                // For example, a grid dimension of 5 gives:
                // 1.1 - ((5 - 3) * 0.12) = 0.86.
                else -> max(
                    0.75f,
                    1.1f - (gridDimension - 3) * 0.12f
                )
            }
        }
    }

    // Create the shared 3D rendering surface.
    SceneView(
        modifier = modifier.fillMaxSize(),
        engine = engine,
        modelLoader = modelLoader,
        mainLightNode = mainLightNode,
        cameraNode = cameraNode,

        // Use a texture-backed surface with transparency so the
        // Compose background can be displayed behind the 3D scene.
        surfaceType = SurfaceType.TextureSurface,
        isOpaque = false,

        // Disable user camera movement and allow SceneView to fit
        // the dice models within the available camera view.
        cameraManipulator = null,
        autoFitContent = true,
        framingPadding = dynamicFramingPadding
    ) {
        // Create a 3D model for every individual die in the scene.
        instances.forEach { diceInstance ->

            // Give each die a stable identity using its unique ID.
            key(diceInstance.id) {

                // Retrieve the model asset and scale for this dice type.
                val modelSpec = DiceModelSpecs.get(
                    diceInstance.diceType
                )

                // Load or retrieve the model instance using its asset path.
                val modelInstance = rememberModelInstance(
                    modelLoader,
                    modelSpec.assetPath
                )

                // Retrieve the calibrated rotation needed to display
                // the result rolled by this particular die.
                val orientation = DiceFaceOrientations.get(
                    diceType = diceInstance.diceType,
                    result = diceInstance.result
                )

                // Calculate the current rotation on each axis.
                // The calibrated final orientation is combined with
                // the extra spin offset used during the animation.
                //
                // For example, if the final X orientation is 90 degrees
                // and the extra spin is 720 degrees, the total is 810.
                // At 50% progress, 405 degrees of rotation are applied.
                val rotation = Rotation(
                    x = progress * (
                            orientation.x + DiceRotationOffsets.X
                            ),
                    y = progress * (
                            orientation.y + DiceRotationOffsets.Y
                            ),
                    z = progress * (
                            orientation.z + DiceRotationOffsets.Z
                            )
                )

                // Keep the die's assigned X and Y grid positions.
                // Adjust its Z position to create the rolling movement.
                val position = Position(
                    x = diceInstance.position.x,
                    y = diceInstance.position.y,
                    z = diceInstance.position.z -
                            // The sine calculation creates a curved movement.
                            // It reaches it's maximum halfway through the animation:
                            // sin(0) = 0, sin(PI / 2) = 1, sin(PI) = 0.
                            // Multiplying by 0.55 controls its size.
                            0.55f * sin(PI.toFloat() * progress) -
                            // Add a gradual linear movement over the animation.
                            // At 50% progress this is 0.075; at 100% it is 0.15.
                            0.15f * progress
                )

                // Only create the model node once the model instance is available.
                // ?.let safely skips this block if the model instance is null.
                modelInstance?.let { instance ->
                    ModelNode(
                        modelInstance = instance,
                        scaleToUnits = modelSpec.scaleToUnits,
                        position = position,
                        rotation = rotation
                    )
                }
            }
        }
    }
}
