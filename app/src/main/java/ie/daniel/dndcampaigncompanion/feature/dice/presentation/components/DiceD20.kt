
package ie.daniel.dndcampaigncompanion.feature.dice.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.sceneview.SceneView
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberMainLightNode
import io.github.sceneview.rememberModelInstance
import io.github.sceneview.rememberModelLoader
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

private data class D20FaceOrientation(
    val x: Float,
    val y: Float,
    val z: Float
)

private val d20FaceOrientations = listOf(
    D20FaceOrientation(109.64f, -7.31f, 139.64f),    // 1
    D20FaceOrientation(70.36f, 7.31f, 79.64f),       // 2
    D20FaceOrientation(120.00f, -48.19f, -165.52f),  // 3
    D20FaceOrientation(37.76f, -19.47f, 23.28f),     // 4
    D20FaceOrientation(46.95f, -60.77f, -43.05f),    // 5
    D20FaceOrientation(142.24f, 19.47f, -156.72f),   // 6
    D20FaceOrientation(60.00f, 48.19f, 14.48f),      // 7
    D20FaceOrientation(133.05f, 60.77f, 136.95f),    // 8
    D20FaceOrientation(-109.64f, -7.31f, 160.36f),   // 9
    D20FaceOrientation(-70.36f, 7.31f, -19.64f),     // 10
    D20FaceOrientation(-120.00f, -48.19f, 105.52f),  // 11
    D20FaceOrientation(-37.76f, -19.47f, 36.72f),    // 12
    D20FaceOrientation(-46.95f, -60.77f, 103.05f),   // 13
    D20FaceOrientation(-142.24f, 19.47f, -143.28f),  // 14
    D20FaceOrientation(-60.00f, 48.19f, -74.48f),    // 15
    D20FaceOrientation(-133.05f, 60.77f, -76.95f),   // 16
    D20FaceOrientation(180.00f, 0.00f, 150.00f),     // 17
    D20FaceOrientation(-180.00f, -41.81f, -150.00f), // 18
    D20FaceOrientation(0.00f, 41.81f, 30.00f),       // 19
    D20FaceOrientation(0.00f, 0.00f, -30.00f)        // 20
)

@Composable
fun DiceD20(
    modifier: Modifier = Modifier
) {
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)

    val modelInstance = rememberModelInstance(
        modelLoader,
        "models/ruby_d20.glb"
    )

    val cameraNode = rememberCameraNode(engine) {
        position = Position(z = 4.5f)
    }

    val animationProgress = remember {
        Animatable(1f)
    }

    val coroutineScope = rememberCoroutineScope()

    var selectedResult by remember {
        mutableIntStateOf(20)
    }

    var displayedResult by remember {
        mutableIntStateOf(20)
    }

    var isRolling by remember {
        mutableStateOf(false)
    }

    val orientation = d20FaceOrientations[displayedResult - 1]
    val progress = animationProgress.value

    val rotation = Rotation(
        x = progress * (orientation.x + 720f),
        y = progress * (orientation.y + 1080f),
        z = progress * (orientation.z + 1440f)
    )

    val position = Position(
        z = -0.55f * sin(PI.toFloat() * progress) -
                0.15f * progress
    )

    Box(modifier = modifier.fillMaxSize()) {

        SceneView(
            modifier = Modifier.fillMaxSize(),
            engine = engine,
            modelLoader = modelLoader,
            mainLightNode = rememberMainLightNode(engine) {
                intensity = 100_000.0f
            },
            cameraNode = cameraNode
        ) {
            modelInstance?.let { instance ->
                ModelNode(
                    modelInstance = instance,
                    scaleToUnits = 1.0f,
                    position = position,
                    rotation = rotation
                )
            }
        }

        // Temporary testing controls.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Surface(
                color = Color(0xDD1A080E),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier.padding(
                        horizontal = 20.dp,
                        vertical = 12.dp
                    ),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "3D D20 Prototype",
                        color = Color(0xFFFFDFA0),
                        fontSize = 20.sp
                    )

                    Text(
                        text = "Testing face orientation",
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 12.sp
                    )
                }
            }

            Surface(
                color = Color(0xEE1A080E),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    Text(
                        text = "Select D20 result",
                        color = Color(0xFFFFDFA0),
                        fontSize = 16.sp
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                if (selectedResult > 1 && !isRolling) {
                                    selectedResult--
                                }
                            },
                            enabled = selectedResult > 1 && !isRolling,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF8D1028)
                            ),
                            modifier = Modifier.size(52.dp)
                        ) {
                            Text("−", fontSize = 24.sp)
                        }

                        Text(
                            text = selectedResult.toString(),
                            color = Color.White,
                            fontSize = 30.sp
                        )

                        Button(
                            onClick = {
                                if (selectedResult < 20 && !isRolling) {
                                    selectedResult++
                                }
                            },
                            enabled = selectedResult < 20 && !isRolling,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF8D1028)
                            ),
                            modifier = Modifier.size(52.dp)
                        ) {
                            Text("+", fontSize = 24.sp)
                        }
                    }

                    Button(
                        onClick = {
                            if (!isRolling) {
                                coroutineScope.launch {
                                    isRolling = true

                                    animationProgress.snapTo(0f)
                                    displayedResult = selectedResult

                                    animationProgress.animateTo(
                                        targetValue = 1f,
                                        animationSpec = tween(
                                            durationMillis = 2200,
                                            easing = FastOutSlowInEasing
                                        )
                                    )

                                    isRolling = false
                                }
                            }
                        },
                        enabled = !isRolling,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFB51E38)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isRolling) "Rolling..." else "Roll D20",
                            fontSize = 17.sp
                        )
                    }

                    Text(
                        text = "Last displayed result: $displayedResult",
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
