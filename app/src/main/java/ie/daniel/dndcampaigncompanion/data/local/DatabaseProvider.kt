package ie.daniel.dndcampaigncompanion.data.local

import android.content.Context
import androidx.room.Room

/**
 * Provides a single shared instance of the Room database.
 *
 * This prevents multiple database instances from being created throughout
 * the application. Other parts of the app can call getDatabase() to access
 * the same database instance.
 */
object DatabaseProvider {

    // Stores the database instance once it has been created.
    // Volatile ensures that changes to this variable are visible across threads.
    @Volatile
    private var INSTANCE: AppDatabase? = null

    // Returns the existing database instance or creates one if it does not exist.
    fun getDatabase(context: Context): AppDatabase {

        // First checks whether an instance already exists.
        // If it does, there is no need to create another one.
        return INSTANCE ?: synchronized(this) {

            // Checks again inside the synchronized block to make sure another
            // thread has not already created the database while waiting.
            // This prevents multiple instances from being created at the same time.
            INSTANCE ?: Room.databaseBuilder(
                // Uses the application context so the database does not hold
                // a reference to an Activity or another short-lived Context.
                context.applicationContext,

                // Specifies the Room database class that should be created.
                AppDatabase::class.java,

                // Name of the database file stored on the device.
                "dnd_campaign_companion_database"

            ).build().also { database ->

                // Saves the newly created database instance so it can be reused.
                INSTANCE = database
            }
        }
    }
}
