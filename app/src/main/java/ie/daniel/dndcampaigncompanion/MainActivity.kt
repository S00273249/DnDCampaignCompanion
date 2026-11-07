
package ie.daniel.dndcampaigncompanion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import ie.daniel.dndcampaigncompanion.feature.dice.presentation.DiceRollScreen
import ie.daniel.dndcampaigncompanion.feature.dice.presentation.DiceRollViewModel
import ie.daniel.dndcampaigncompanion.ui.theme.DnDCampaignCompanionTheme

// Main Android activity and entry point for the application.
class MainActivity : ComponentActivity() {

    // Called when Android creates the activity.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Allow the application content to extend behind the system bars.
        enableEdgeToEdge()

        // Set up the Jetpack Compose interface for this activity.
        setContent {

            // Apply the application's Material theme, including its colours and typography.
            DnDCampaignCompanionTheme {

                // Provides the main layout structure for the screen.
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->

                    // Obtain the DiceRollViewModel associated with this activity.
                    // An existing instance is reused if one is already available.
                    val diceRollViewModel: DiceRollViewModel = viewModel()

                    // Apply the padding provided by Scaffold to the content container.
                    // This ensures the screen is positioned within the available content area.
                    Box(
                        modifier = Modifier.padding(innerPadding)
                    ) {

                        // Display the dice roller screen using the ViewModel.
                        DiceRollScreen(
                            viewModel = diceRollViewModel
                        )
                    }
                }
            }
        }
    }
}
