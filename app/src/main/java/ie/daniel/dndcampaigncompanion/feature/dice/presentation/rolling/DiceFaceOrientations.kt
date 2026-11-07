
package ie.daniel.dndcampaigncompanion.feature.dice.presentation.rolling

import ie.daniel.dndcampaigncompanion.feature.dice.domain.DiceType

// Stores the X, Y and Z rotations needed to display a particular dice result.
data class DiceFaceOrientation(
    val x: Float,
    val y: Float,
    val z: Float
)

// Extra rotations applied during the rolling animation.
// All dice use the same rotation offsets.
object DiceRotationOffsets {
    const val X = 720f
    const val Y = 1080f
    const val Z = 1440f
}

// Stores the calibrated final orientations for every dice type.
// Each list is ordered by dice result, starting at 1.
object DiceFaceOrientations {

    private val orientations: Map<DiceType, List<DiceFaceOrientation>> =
        mapOf(

            DiceType.D4 to listOf(
                DiceFaceOrientation(45f, 35.264f, 90f),       // 1
                DiceFaceOrientation(-45f, -35.264f, 30f),     // 2
                DiceFaceOrientation(135f, -35.264f, 150f),    // 3
                DiceFaceOrientation(-135f, 35.264f, -30f)     // 4
            ),

            DiceType.D6 to listOf(
                DiceFaceOrientation(20f, 160f, 0f),   // 1
                DiceFaceOrientation(-70f, 0f, 20f),   // 2
                DiceFaceOrientation(10f, 70f, 0f),    // 3
                DiceFaceOrientation(10f, -110f, 0f),  // 4
                DiceFaceOrientation(100f, 0f, 160f),   // 5
                DiceFaceOrientation(10f, -20f, 0f)    // 6
            ),

            DiceType.D8 to listOf(
                DiceFaceOrientation(45f, -35.264f, 180f),     // 1
                DiceFaceOrientation(45f, 35.264f, 180f),      // 2
                DiceFaceOrientation(-45f, 35.264f, 0f),       // 3
                DiceFaceOrientation(-45f, -35.264f, 0f),      // 4
                DiceFaceOrientation(135f, -35.264f, 0f),      // 5
                DiceFaceOrientation(135f, 35.264f, 0f),       // 6
                DiceFaceOrientation(-135f, 35.264f, 180f),    // 7
                DiceFaceOrientation(-135f, -35.264f, 180f)    // 8
            ),

            DiceType.D10 to listOf(
                DiceFaceOrientation(39.35f, 24.73f, 152.97f),       // 1
                DiceFaceOrientation(-17.39f, 42.61f, 65.17f),      // 2
                DiceFaceOrientation(-45.39f, 0f, 0f),              // 3
                DiceFaceOrientation(-17.39f, -42.61f, -65.17f),    // 4
                DiceFaceOrientation(39.35f, -24.73f, -152.97f),   // 5
                DiceFaceOrientation(140.65f, 24.73f, 27.03f),     // 6
                DiceFaceOrientation(-162.61f, 42.61f, 114.83f),   // 7
                DiceFaceOrientation(-134.61f, 0f, 180f),          // 8
                DiceFaceOrientation(-162.61f, -42.61f, -114.83f),// 9
                DiceFaceOrientation(140.65f, -24.73f, -27.03f)   // 10
            ),

            DiceType.D12 to listOf(
                DiceFaceOrientation(-31.72f, 0f, 198f),       // 1
                DiceFaceOrientation(31.72f, 0f, 162f),        // 2
                DiceFaceOrientation(0f, -58.28f, 216f),       // 3
                DiceFaceOrientation(0f, 58.28f, 252f),        // 4
                DiceFaceOrientation(-90f, 31.72f, 144f),      // 5
                DiceFaceOrientation(-90f, -31.72f, 252f),     // 6
                DiceFaceOrientation(90f, -31.72f, 108f),      // 7
                DiceFaceOrientation(90f, 31.72f, 72f),        // 8
                DiceFaceOrientation(-180f, -58.28f, 72f),     // 9
                DiceFaceOrientation(-180f, 58.28f, 36f),      // 10
                DiceFaceOrientation(-148.28f, 0f, 90f),       // 11
                DiceFaceOrientation(148.28f, 0f, 54f)         // 12
            ),

            DiceType.D20 to listOf(
                DiceFaceOrientation(109.64f, -7.31f, 139.64f),     // 1
                DiceFaceOrientation(70.36f, 7.31f, 79.64f),       // 2
                DiceFaceOrientation(120.00f, -48.19f, -165.52f),  // 3
                DiceFaceOrientation(37.76f, -19.47f, 23.28f),     // 4
                DiceFaceOrientation(46.95f, -60.77f, -43.05f),    // 5
                DiceFaceOrientation(142.24f, 19.47f, -156.72f),   // 6
                DiceFaceOrientation(60.00f, 48.19f, 14.48f),      // 7
                DiceFaceOrientation(133.05f, 60.77f, 136.95f),    // 8
                DiceFaceOrientation(-109.64f, -7.31f, 160.36f),   // 9
                DiceFaceOrientation(-70.36f, 7.31f, -19.64f),     // 10
                DiceFaceOrientation(-120.00f, -48.19f, 105.52f),  // 11
                DiceFaceOrientation(-37.76f, -19.47f, 36.72f),    // 12
                DiceFaceOrientation(-46.95f, -60.77f, 103.05f),   // 13
                DiceFaceOrientation(-142.24f, 19.47f, -143.28f),  // 14
                DiceFaceOrientation(-60.00f, 48.19f, -74.48f),    // 15
                DiceFaceOrientation(-133.05f, 60.77f, -76.95f),   // 16
                DiceFaceOrientation(180.00f, 0.00f, 150.00f),     // 17
                DiceFaceOrientation(-180.00f, -41.81f, -150.00f), // 18
                DiceFaceOrientation(0.00f, 41.81f, 30.00f),       // 19
                DiceFaceOrientation(0.00f, 0.00f, -30.00f)        // 20
            ),

            DiceType.D100 to listOf(
                DiceFaceOrientation(90.0000f, -8.1096f, 270.0000f), // 1
                DiceFaceOrientation(80.3913f, 10.3265f, 270.0000f), // 2
                DiceFaceOrientation(108.1298f, -1.5643f, 270.0000f), // 3
                DiceFaceOrientation(72.5859f, -12.9228f, 270.0000f), // 4
                DiceFaceOrientation(94.5375f, 24.0962f, 270.0000f), // 5
                DiceFaceOrientation(105.3749f, -22.6263f, 54.4451f), // 6
                DiceFaceOrientation(61.3083f, 7.3539f, 193.1634f), // 7
                DiceFaceOrientation(118.8104f, 14.0517f, 336.1806f), // 8
                DiceFaceOrientation(77.0188f, -31.5955f, 113.7494f), // 9
                DiceFaceOrientation(74.5576f, 32.8242f, 242.9961f), // 10
                DiceFaceOrientation(125.1044f, -15.0619f, 20.2887f), // 11
                DiceFaceOrientation(51.6684f, -11.0086f, 166.4220f), // 12
                DiceFaceOrientation(113.8550f, 34.9096f, 307.6930f), // 13
                DiceFaceOrientation(101.3665f, -41.8749f, 73.2398f), // 14
                DiceFaceOrientation(50.9448f, 23.8916f, 206.5264f), // 15
                DiceFaceOrientation(136.1316f, 5.3372f, 354.8906f), // 16
                DiceFaceOrientation(54.4713f, -34.5862f, 141.5164f), // 17
                DiceFaceOrientation(87.2344f, 49.4012f, 266.3597f), // 18
                DiceFaceOrientation(131.0075f, -33.3995f, 32.3371f), // 19
                DiceFaceOrientation(37.6191f, 2.0976f, 181.6157f), // 20
                DiceFaceOrientation(136.4162f, 31.1520f, 333.7867f), // 21
                DiceFaceOrientation(79.1195f, -54.5189f, 103.2811f), // 22
                DiceFaceOrientation(49.0664f, 43.2790f, 218.3259f), // 23
                DiceFaceOrientation(147.3555f, -10.7264f, 6.7994f), // 24
                DiceFaceOrientation(34.3466f, -25.3193f, 163.7095f), // 25
                DiceFaceOrientation(118.4004f, 56.1485f, 303.0672f), // 26
                DiceFaceOrientation(128.2268f, -53.2522f, 45.4902f), // 27
                DiceFaceOrientation(28.6457f, 20.1674f, 190.6655f), // 28
                DiceFaceOrientation(153.1537f, 17.7921f, 351.2082f), // 29
                DiceFaceOrientation(44.0160f, -53.8396f, 142.0421f), // 30
                DiceFaceOrientation(58.9600f, 62.9236f, 235.9457f), // 31
                DiceFaceOrientation(154.6630f, -30.1626f, 13.3823f), // 32
                DiceFaceOrientation(20.7622f, -9.1293f, 176.5578f), // 33
                DiceFaceOrientation(150.2766f, 48.2737f, 336.9209f), // 34
                DiceFaceOrientation(104.2097f, -71.3501f, 75.0373f), // 35
                DiceFaceOrientation(22.4308f, 40.5338f, 195.0175f), // 36
                DiceFaceOrientation(164.3356f, -0.2691f, 0.0755f), // 37
                DiceFaceOrientation(19.2190f, -40.5831f, 167.2222f), // 38
                DiceFaceOrientation(111.3296f, 75.7051f, 291.9472f), // 39
                DiceFaceOrientation(160.4408f, -51.1506f, 15.4663f), // 40
                DiceFaceOrientation(11.1281f, 10.1207f, 181.9796f), // 41
                DiceFaceOrientation(168.4794f, 31.6583f, 353.8938f), // 42
                DiceFaceOrientation(29.8560f, -72.4636f, 151.3069f), // 43
                DiceFaceOrientation(16.0223f, 61.9012f, 194.2152f), // 44
                DiceFaceOrientation(173.2684f, -20.2128f, 2.3353f), // 45
                DiceFaceOrientation(5.5726f, -22.0560f, 177.9017f), // 46
                DiceFaceOrientation(170.6945f, 64.3479f, 351.5979f), // 47
                DiceFaceOrientation(170.3564f, -72.6340f, 9.2117f), // 48
                DiceFaceOrientation(1.9924f, 30.3576f, 181.0073f), // 49
                DiceFaceOrientation(179.4140f, 12.1189f, 359.8770f), // 50
                DiceFaceOrientation(-0.9893f, -54.6078f, 180.8065f), // 51
                DiceFaceOrientation(-13.6413f, 82.6920f, 166.4655f), // 52
                DiceFaceOrientation(-176.2386f, -40.3428f, 357.5630f), // 53
                DiceFaceOrientation(-4.0166f, -2.0834f, 180.1463f), // 54
                DiceFaceOrientation(-172.7694f, 44.3520f, 5.0684f), // 55
                DiceFaceOrientation(-65.2314f, -83.0418f, 245.0699f), // 56
                DiceFaceOrientation(-11.6314f, 49.8499f, 171.0585f), // 57
                DiceFaceOrientation(-171.2908f, -7.8521f, 358.8011f), // 58
                DiceFaceOrientation(-11.8296f, -33.9774f, 186.6764f), // 59
                DiceFaceOrientation(-139.2047f, 73.0938f, 39.5477f), // 60
                DiceFaceOrientation(-156.4558f, -58.2836f, 339.6624f), // 61
                DiceFaceOrientation(-13.9532f, 17.4760f, 175.7332f), // 62
                DiceFaceOrientation(-164.1566f, 23.6915f, 6.5053f), // 63
                DiceFaceOrientation(-35.6779f, -62.4229f, 212.4733f), // 64
                DiceFaceOrientation(-42.2281f, 64.4376f, 140.6896f), // 65
                DiceFaceOrientation(-159.7306f, -26.5137f, 350.6383f), // 66
                DiceFaceOrientation(-19.8529f, -13.6598f, 184.8737f), // 67
                DiceFaceOrientation(-145.5637f, 51.7618f, 28.3041f), // 68
                DiceFaceOrientation(-112.4509f, -66.4004f, 294.2717f), // 69
                DiceFaceOrientation(-28.2687f, 34.5665f, 163.0334f), // 70
                DiceFaceOrientation(-155.7302f, 4.0640f, 1.8302f), // 71
                DiceFaceOrientation(-34.9032f, -41.2798f, 204.7163f), // 72
                DiceFaceOrientation(-91.1092f, 63.2509f, 88.7578f), // 73
                DiceFaceOrientation(-141.4519f, -41.0439f, 332.3809f), // 74
                DiceFaceOrientation(-29.4567f, 4.8576f, 177.2620f), // 75
                DiceFaceOrientation(-143.4396f, 31.1097f, 20.9651f), // 76
                DiceFaceOrientation(-73.6141f, -56.4656f, 250.5689f), // 77
                DiceFaceOrientation(-51.2539f, 45.1548f, 138.5387f), // 78
                DiceFaceOrientation(-144.2354f, -12.7698f, 350.9540f), // 79
                DiceFaceOrientation(-39.3278f, -21.4155f, 196.6546f), // 80
                DiceFaceOrientation(-114.5840f, 47.8706f, 58.3302f), // 81
                DiceFaceOrientation(-114.6647f, -46.1122f, 302.5037f), // 82
                DiceFaceOrientation(-43.4931f, 19.1957f, 162.6753f), // 83
                DiceFaceOrientation(-136.6791f, 12.4305f, 11.4748f), // 84
                DiceFaceOrientation(-61.8619f, -38.5119f, 229.3413f), // 85
                DiceFaceOrientation(-78.4971f, 43.5683f, 106.4502f), // 86
                DiceFaceOrientation(-127.2568f, -23.4849f, 332.3484f), // 87
                DiceFaceOrientation(-48.7924f, -4.5080f, 185.1290f), // 88
                DiceFaceOrientation(-118.3763f, 28.9368f, 41.8518f), // 89
                DiceFaceOrientation(-91.4034f, -37.7923f, 272.2894f), // 90
                DiceFaceOrientation(-63.1763f, 24.8153f, 140.3073f), // 91
                DiceFaceOrientation(-123.8597f, -1.7878f, 357.3377f), // 92
                DiceFaceOrientation(-64.3739f, -19.4845f, 214.8133f), // 93
                DiceFaceOrientation(-94.6336f, 29.2081f, 80.5700f), // 94
                DiceFaceOrientation(-106.0919f, -22.1357f, 307.4380f), // 95
                DiceFaceOrientation(-66.0824f, 5.4479f, 270.0000f), // 96
                DiceFaceOrientation(-109.0251f, 10.3478f, 270.0000f), // 97
                DiceFaceOrientation(-84.1222f, -17.2498f, 270.0000f), // 98
                DiceFaceOrientation(-84.1254f, 12.8073f, 270.0000f), // 99
                DiceFaceOrientation(-97.4577f, -3.1946f, 270.0000f) // 100
            )
        )

    /**
     * Returns the calibrated orientation for a particular
     * dice type and result.
     *
     * @throws IllegalArgumentException if the result is outside
     * the valid range for the selected die.
     */
    fun get(
        diceType: DiceType,
        result: Int
    ): DiceFaceOrientation {

        // Find the orientation list for the selected dice type.
        // Throw an exception if no orientations have been configured.
        val faces = orientations[diceType]
            ?: throw IllegalArgumentException(
                "No face orientations configured for $diceType"
            )

        // Make sure the requested result is valid for this dice type.
        require(result in 1..faces.size) {
            "Result $result is invalid for $diceType. " +
                    "Valid results are 1..${faces.size}."
        }

        // Lists start at index 0, so subtract 1 from the dice result.
        return faces[result - 1]
    }

    /**
     * Returns the number of calibrated faces for a die type.
     */
    fun faceCount(diceType: DiceType): Int =
        orientations[diceType]?.size ?: 0
}
