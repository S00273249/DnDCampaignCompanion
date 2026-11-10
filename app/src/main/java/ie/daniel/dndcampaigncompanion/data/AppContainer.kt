package ie.daniel.dndcampaigncompanion.data

import android.content.Context
import ie.daniel.dndcampaigncompanion.data.local.ActiveProfileDataStore
import ie.daniel.dndcampaigncompanion.data.local.DatabaseProvider
import ie.daniel.dndcampaigncompanion.data.repository.CampaignCharacterRepository
import ie.daniel.dndcampaigncompanion.data.repository.CampaignMembershipRepository
import ie.daniel.dndcampaigncompanion.data.repository.CampaignPhotoRepository
import ie.daniel.dndcampaigncompanion.data.repository.CampaignRepository
import ie.daniel.dndcampaigncompanion.data.repository.CharacterRepository
import ie.daniel.dndcampaigncompanion.data.repository.DiceRollRepository
import ie.daniel.dndcampaigncompanion.data.repository.SessionNoteRepository
import ie.daniel.dndcampaigncompanion.data.repository.SessionRepository
import ie.daniel.dndcampaigncompanion.data.repository.UserProfileRepository

/**
 * Provides the application's data-related dependencies.
 *
 * This container creates the database, DataStore and repositories used
 * throughout the application. Other parts of the app can access these
 * dependencies through a single shared container instead of creating
 * their own instances.
 */
class AppContainer(context: Context) {

    // Retrieves the application's Room database instance.
    // DatabaseProvider ensures the database is shared rather than recreated
    // whenever a repository needs access to it.
    private val database = DatabaseProvider.getDatabase(context)

    // Provides access to the ID of the currently selected user profile.
    // This information is stored using Preferences DataStore.
    val activeProfileDataStore = ActiveProfileDataStore(context)

    // Handles creating, retrieving, updating and deleting user profiles.
    val userProfileRepository = UserProfileRepository(
        database.userProfileDao()
    )

    // Handles campaign creation, retrieval, editing and status changes.
    val campaignRepository = CampaignRepository(database)

    // Handles campaign memberships, including joining and leaving campaigns
    // and managing player membership.
    val campaignMembershipRepository =
        CampaignMembershipRepository(database)

    // Handles the user's saved characters and their details.
    val characterRepository = CharacterRepository(
        database.characterDao()
    )

    // Handles assigning saved characters to campaigns and managing
    // their campaign-specific character information.
    val campaignCharacterRepository =
        CampaignCharacterRepository(database)

    // Handles scheduling and managing campaign sessions.
    val sessionRepository = SessionRepository(database)

    // Handles creating, retrieving, updating and deleting session notes.
    // The required DAOs are passed in so the repository can access
    // notes, sessions and campaign membership information.
    val sessionNoteRepository = SessionNoteRepository(
        database.sessionNoteDao(),
        database.sessionDao(),
        database.campaignMembershipDao()
    )

    // Handles saving and retrieving dice roll history.
    val diceRollRepository = DiceRollRepository(database)

    // Handles campaign photo uploads, captions and deletion.
    // The membership DAO is also provided for checking user permissions.
    val campaignPhotoRepository = CampaignPhotoRepository(
        database.campaignPhotoDao(),
        database.campaignMembershipDao()
    )
}
