
package ie.daniel.dndcampaigncompanion.feature.dice.presentation.rolling

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Detects deliberate phone shakes while enabled.
 *
 * Reports whether an accelerometer is available and uses
 * acceleration relative to gravity to detect shakes.
 * A cooldown prevents repeated roll triggers.
 */
@Composable
fun ShakeDetector(
    enabled: Boolean,
    onShakeDetected: () -> Unit,
    onAccelerometerAvailabilityChanged: (Boolean) -> Unit
) {
    // Retrieve the Android context so the device's sensor service
    // can be accessed.
    val context = LocalContext.current

    // Manage the sensor listener's lifecycle.
    // The effect restarts if one of its keys changes.
    DisposableEffect(
        context,
        enabled,
        onShakeDetected,
        onAccelerometerAvailabilityChanged
    ) {
        // Only check and activate the sensor when shake detection is enabled.
        if (!enabled) {
            onDispose { }
        } else {

            // Retrieve Android's sensor manager from the system services.
            val sensorManager = context.getSystemService(
                Context.SENSOR_SERVICE
            ) as SensorManager

            // Request the device's default accelerometer.
            val accelerometer = sensorManager.getDefaultSensor(
                Sensor.TYPE_ACCELEROMETER
            )

            // Report whether an accelerometer is available.
            onAccelerometerAvailabilityChanged(accelerometer != null)

            // If no accelerometer is available, there is no listener to register.
            if (accelerometer == null) {
                onDispose { }
            } else {

                // Stores the time of the most recently detected shake.
                var lastShakeTime = 0L

                // Minimum acceleration difference required to detect a shake.
                val shakeThreshold = 3.0f

                // Minimum time between accepted shakes (1.2 seconds).
                val cooldownMillis = 1200L

                // Create a listener to receive accelerometer readings.
                val listener = object : SensorEventListener {

                    override fun onSensorChanged(event: SensorEvent?) {

                        // Ignore the reading if no sensor event was provided.
                        if (event == null) return

                        // Read acceleration along the three device axes.
                        val x = event.values[0]
                        val y = event.values[1]
                        val z = event.values[2]

                        // Calculate the overall acceleration magnitude using
                        // the three-dimensional Pythagorean theorem.
                        //
                        // For example, x = 3, y = -4 and z = 9 gives:
                        // sqrt(3² + (-4)² + 9²)
                        // = sqrt(9 + 16 + 81)
                        // = sqrt(106)
                        // = approximately 10.30 m/s².
                        val accelerationMagnitude = sqrt(
                            x * x + y * y + z * z
                        )

                        // Subtract Earth's gravity to estimate acceleration
                        // caused by movement rather than gravity alone.
                        // abs() ensures the difference is always positive.
                        val accelerationWithoutGravity = abs(
                            accelerationMagnitude -
                                    SensorManager.GRAVITY_EARTH
                        )

                        // Get the current time in milliseconds.
                        val currentTime =
                            System.currentTimeMillis()

                        // Both conditions must be satisfied:
                        // 1. Acceleration must exceed the shake threshold.
                        // 2. The cooldown period must have elapsed.
                        if (
                            accelerationWithoutGravity > shakeThreshold &&
                            currentTime - lastShakeTime >= cooldownMillis
                        ) {
                            // Record when the shake was detected.
                            lastShakeTime = currentTime

                            // Notify the dice roller that a shake was detected.
                            onShakeDetected()
                        }
                    }

                    // Called when the sensor's accuracy changes.
                    override fun onAccuracyChanged(
                        sensor: Sensor?,
                        accuracy: Int
                    ) {
                        // No accuracy adjustment required.
                    }
                }

                // Register the listener to start receiving accelerometer readings.
                sensorManager.registerListener(
                    listener,
                    accelerometer,
                    SensorManager.SENSOR_DELAY_GAME
                )

                // Unregister the listener when the effect is disposed.
                onDispose {
                    sensorManager.unregisterListener(listener)
                }
            }
        }
    }
}
