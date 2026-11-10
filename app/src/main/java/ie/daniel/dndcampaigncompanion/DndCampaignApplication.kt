package ie.daniel.dndcampaigncompanion

import android.app.Application
import ie.daniel.dndcampaigncompanion.data.AppContainer

/**
 * Custom Application class for D&D Campaign Companion.
 *
 * Provides a central place to initialise application-wide dependencies
 * that need to be shared across different activities and components.
 */
class DndCampaignApplication : Application() {

    // Holds the shared AppContainer used throughout the application.
    // lateinit allows it to be initialised when the application starts,
    // rather than requiring an instance when the class is first created.
    // The private setter prevents other classes from replacing the container.
    lateinit var appContainer: AppContainer
        private set

    // Called by Android when the application process is created.
    override fun onCreate() {
        super.onCreate()

        // Creates the shared dependency container.
        // Passing the Application context allows the container to initialise
        // the database, DataStore and repositories for the application.
        appContainer = AppContainer(this)
    }
}
