
package ie.daniel.dndcampaigncompanion.feature.dice.presentation.rolling

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import ie.daniel.dndcampaigncompanion.R

// Displays the background image used behind the dice rolling scene.
@Composable
fun DiceRollingBackground(
    modifier: Modifier = Modifier
) {
    Image(
        // Load the background image from the app's drawable resources.
        painter = painterResource(
            id = R.drawable.dice_rolling_background2
        ),
        // The image is decorative, so it doesn't need an accessibility description.
        contentDescription = null,
        // Fill the available space, using any modifier supplied by the caller.
        modifier = modifier.fillMaxSize(),
        // Cover the available area while keeping the image's original proportions.
        // Any excess image area is cropped rather than leaving empty space.
        contentScale = ContentScale.Crop
    )
}
