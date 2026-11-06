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

private data class D100FaceOrientation(
    val x: Float,
    val y: Float,
    val z: Float
)

// Initial orientations calculated from the generated D100 mesh.
// Each entry corresponds to the matching numbered face in ruby_d100.glb.
private val d100FaceOrientations = listOf(
    D100FaceOrientation(90.0000f, -8.1096f, 90.0000f), // 1
    D100FaceOrientation(80.3913f, 10.3265f, 90.0000f), // 2
    D100FaceOrientation(108.1298f, -1.5643f, 90.0000f), // 3
    D100FaceOrientation(72.5859f, -12.9228f, 90.0000f), // 4
    D100FaceOrientation(94.5375f, 24.0962f, 90.0000f), // 5
    D100FaceOrientation(105.3749f, -22.6263f, -125.5549f), // 6
    D100FaceOrientation(61.3083f, 7.3539f, 13.1634f), // 7
    D100FaceOrientation(118.8104f, 14.0517f, 156.1806f), // 8
    D100FaceOrientation(77.0188f, -31.5955f, -66.2506f), // 9
    D100FaceOrientation(74.5576f, 32.8242f, 62.9961f), // 10
    D100FaceOrientation(125.1044f, -15.0619f, -159.7113f), // 11
    D100FaceOrientation(51.6684f, -11.0086f, -13.5780f), // 12
    D100FaceOrientation(113.8550f, 34.9096f, 127.6930f), // 13
    D100FaceOrientation(101.3665f, -41.8749f, -106.7602f), // 14
    D100FaceOrientation(50.9448f, 23.8916f, 26.5264f), // 15
    D100FaceOrientation(136.1316f, 5.3372f, 174.8906f), // 16
    D100FaceOrientation(54.4713f, -34.5862f, -38.4836f), // 17
    D100FaceOrientation(87.2344f, 49.4012f, 86.3597f), // 18
    D100FaceOrientation(131.0075f, -33.3995f, -147.6629f), // 19
    D100FaceOrientation(37.6191f, 2.0976f, 1.6157f), // 20
    D100FaceOrientation(136.4162f, 31.1520f, 153.7867f), // 21
    D100FaceOrientation(79.1195f, -54.5189f, -76.7184f), // 22
    D100FaceOrientation(49.0664f, 43.2790f, 38.3259f), // 23
    D100FaceOrientation(147.3555f, -10.7264f, -173.2006f), // 24
    D100FaceOrientation(34.3466f, -25.3193f, -16.2905f), // 25
    D100FaceOrientation(118.4004f, 56.1485f, 123.0672f), // 26
    D100FaceOrientation(128.2268f, -53.2522f, -134.5098f), // 27
    D100FaceOrientation(28.6457f, 20.1674f, 10.6655f), // 28
    D100FaceOrientation(153.1537f, 17.7921f, 171.2082f), // 29
    D100FaceOrientation(44.0160f, -53.8396f, -37.9579f), // 30
    D100FaceOrientation(58.9600f, 62.9236f, 55.9457f), // 31
    D100FaceOrientation(154.6630f, -30.1626f, -166.6177f), // 32
    D100FaceOrientation(20.7622f, -9.1293f, -3.4422f), // 33
    D100FaceOrientation(150.2766f, 48.2737f, 156.9209f), // 34
    D100FaceOrientation(104.2097f, -71.3501f, -104.9627f), // 35
    D100FaceOrientation(22.4308f, 40.5338f, 15.0175f), // 36
    D100FaceOrientation(164.3356f, -0.2691f, -179.9245f), // 37
    D100FaceOrientation(19.2190f, -40.5831f, -12.7778f), // 38
    D100FaceOrientation(111.3296f, 75.7051f, 111.9472f), // 39
    D100FaceOrientation(160.4408f, -51.1506f, -164.5336f), // 40
    D100FaceOrientation(11.1281f, 10.1207f, 1.9796f), // 41
    D100FaceOrientation(168.4794f, 31.6583f, 173.8938f), // 42
    D100FaceOrientation(29.8560f, -72.4636f, -28.6931f), // 43
    D100FaceOrientation(16.0223f, 61.9012f, 14.2152f), // 44
    D100FaceOrientation(173.2684f, -20.2128f, -177.6647f), // 45
    D100FaceOrientation(5.5726f, -22.0560f, -2.0983f), // 46
    D100FaceOrientation(170.6945f, 64.3479f, 171.5979f), // 47
    D100FaceOrientation(170.3564f, -72.6340f, -170.7883f), // 48
    D100FaceOrientation(1.9924f, 30.3576f, 1.0073f), // 49
    D100FaceOrientation(179.4140f, 12.1189f, 179.8770f), // 50
    D100FaceOrientation(-0.9893f, -54.6078f, 0.8065f), // 51
    D100FaceOrientation(-13.6413f, 82.6920f, -13.5345f), // 52
    D100FaceOrientation(-176.2386f, -40.3428f, 177.5630f), // 53
    D100FaceOrientation(-4.0166f, -2.0834f, 0.1463f), // 54
    D100FaceOrientation(-172.7694f, 44.3520f, -174.9316f), // 55
    D100FaceOrientation(-65.2314f, -83.0418f, 65.0699f), // 56
    D100FaceOrientation(-11.6314f, 49.8499f, -8.9415f), // 57
    D100FaceOrientation(-171.2908f, -7.8521f, 178.8011f), // 58
    D100FaceOrientation(-11.8296f, -33.9774f, 6.6764f), // 59
    D100FaceOrientation(-139.2047f, 73.0938f, -140.4523f), // 60
    D100FaceOrientation(-156.4558f, -58.2836f, 159.6624f), // 61
    D100FaceOrientation(-13.9532f, 17.4760f, -4.2672f), // 62
    D100FaceOrientation(-164.1566f, 23.6915f, -173.4947f), // 63
    D100FaceOrientation(-35.6779f, -62.4229f, 32.4733f), // 64
    D100FaceOrientation(-42.2281f, 64.4376f, -39.3104f), // 65
    D100FaceOrientation(-159.7306f, -26.5137f, 170.6383f), // 66
    D100FaceOrientation(-19.8529f, -13.6598f, 4.8737f), // 67
    D100FaceOrientation(-145.5637f, 51.7618f, -151.6959f), // 68
    D100FaceOrientation(-112.4509f, -66.4004f, 114.2717f), // 69
    D100FaceOrientation(-28.2687f, 34.5665f, -16.9666f), // 70
    D100FaceOrientation(-155.7302f, 4.0640f, -178.1698f), // 71
    D100FaceOrientation(-34.9032f, -41.2798f, 24.7163f), // 72
    D100FaceOrientation(-91.1092f, 63.2509f, -91.2421f), // 73
    D100FaceOrientation(-141.4519f, -41.0439f, 152.3809f), // 74
    D100FaceOrientation(-29.4567f, 4.8576f, -2.7381f), // 75
    D100FaceOrientation(-143.4396f, 31.1097f, -159.0349f), // 76
    D100FaceOrientation(-73.6141f, -56.4656f, 70.5689f), // 77
    D100FaceOrientation(-51.2539f, 45.1548f, -41.4618f), // 78
    D100FaceOrientation(-144.2354f, -12.7698f, 170.9540f), // 79
    D100FaceOrientation(-39.3278f, -21.4155f, 16.6546f), // 80
    D100FaceOrientation(-114.5840f, 47.8706f, -121.6696f), // 81
    D100FaceOrientation(-114.6647f, -46.1122f, 122.5037f), // 82
    D100FaceOrientation(-43.4931f, 19.1957f, -17.3247f), // 83
    D100FaceOrientation(-136.6791f, 12.4305f, -168.5252f), // 84
    D100FaceOrientation(-61.8619f, -38.5119f, 49.3413f), // 85
    D100FaceOrientation(-78.4971f, 43.5683f, -73.5498f), // 86
    D100FaceOrientation(-127.2568f, -23.4849f, 152.3484f), // 87
    D100FaceOrientation(-48.7924f, -4.5080f, 5.1290f), // 88
    D100FaceOrientation(-118.3763f, 28.9368f, -138.1480f), // 89
    D100FaceOrientation(-91.4034f, -37.7923f, 92.2894f), // 90
    D100FaceOrientation(-63.1763f, 24.8153f, -39.6927f), // 91
    D100FaceOrientation(-123.8597f, -1.7878f, 177.3377f), // 92
    D100FaceOrientation(-64.3739f, -19.4845f, 34.8133f), // 93
    D100FaceOrientation(-94.6336f, 29.2081f, -99.4300f), // 94
    D100FaceOrientation(-106.0919f, -22.1357f, 127.4380f), // 95
    D100FaceOrientation(-66.0824f, 5.4479f, 90.0000f), // 96
    D100FaceOrientation(-109.0251f, 10.3478f, 90.0000f), // 97
    D100FaceOrientation(-84.1222f, -17.2498f, 90.0000f), // 98
    D100FaceOrientation(-84.1254f, 12.8073f, 90.0000f), // 99
    D100FaceOrientation(-97.4577f, -3.1946f, 90.0000f) // 100
)

@Composable
fun DiceD100(
    modifier: Modifier = Modifier
) {
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)

    val modelInstance = rememberModelInstance(
        modelLoader,
        "models/ruby_d100.glb"
    )

    val cameraNode = rememberCameraNode(engine) {
        position = Position(z = 4.5f)
    }

    val animationProgress = remember {
        Animatable(1f)
    }

    val coroutineScope = rememberCoroutineScope()

    var selectedResult by remember {
        mutableIntStateOf(100)
    }

    var displayedResult by remember {
        mutableIntStateOf(100)
    }

    var isRolling by remember {
        mutableStateOf(false)
    }

    val orientation = d100FaceOrientations[displayedResult - 1]
    val progress = animationProgress.value

    val rotation = Rotation(
        x = progress * (orientation.x + 720f),
        y = progress * (orientation.y + 1080f),
        z = progress * (orientation.z + 1620f)
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
                        text = "3D D100 Prototype",
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
                        text = "Select D100 result",
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
                                if (selectedResult < 100 && !isRolling) {
                                    selectedResult++
                                }
                            },
                            enabled = selectedResult < 100 && !isRolling,
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
                            text = if (isRolling) "Rolling..." else "Roll D100",
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
