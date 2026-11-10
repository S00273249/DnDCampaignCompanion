package ie.daniel.dndcampaigncompanion.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ie.daniel.dndcampaigncompanion.data.local.converter.Converters
import ie.daniel.dndcampaigncompanion.data.local.dao.CampaignCharacterDao
import ie.daniel.dndcampaigncompanion.data.local.dao.CampaignDao
import ie.daniel.dndcampaigncompanion.data.local.dao.CampaignMembershipDao
import ie.daniel.dndcampaigncompanion.data.local.dao.CampaignPhotoDao
import ie.daniel.dndcampaigncompanion.data.local.dao.CharacterDao
import ie.daniel.dndcampaigncompanion.data.local.dao.DiceRollDao
import ie.daniel.dndcampaigncompanion.data.local.dao.SessionDao
import ie.daniel.dndcampaigncompanion.data.local.dao.SessionNoteDao
import ie.daniel.dndcampaigncompanion.data.local.dao.UserProfileDao
import ie.daniel.dndcampaigncompanion.data.local.entity.CampaignCharacterEntity
import ie.daniel.dndcampaigncompanion.data.local.entity.CampaignEntity
import ie.daniel.dndcampaigncompanion.data.local.entity.CampaignMembershipEntity
import ie.daniel.dndcampaigncompanion.data.local.entity.CampaignPhotoEntity
import ie.daniel.dndcampaigncompanion.data.local.entity.CharacterEntity
import ie.daniel.dndcampaigncompanion.data.local.entity.DiceRollEntity
import ie.daniel.dndcampaigncompanion.data.local.entity.SessionEntity
import ie.daniel.dndcampaigncompanion.data.local.entity.SessionNoteEntity
import ie.daniel.dndcampaigncompanion.data.local.entity.UserProfileEntity

/**
 * Main Room database for the application.
 *
 * This class brings together all database entities and provides access
 * to their corresponding DAOs. It also registers the type converters
 * needed to store enum values in the database.
 */
@Database(
    // Lists all entities that Room should create database tables for.
    entities = [
        UserProfileEntity::class,
        CampaignEntity::class,
        CampaignMembershipEntity::class,
        CharacterEntity::class,
        CampaignCharacterEntity::class,
        SessionEntity::class,
        SessionNoteEntity::class,
        DiceRollEntity::class,
        CampaignPhotoEntity::class
    ],
    // Database version used by Room to track changes to the database structure.
    // This is currently the first version of the database.
    version = 1,

    // Disables exporting the database schema to a separate file.
    exportSchema = false
)
// Registers the converters that allow Room to store enum values as Strings.
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    // Provides access to database operations for user profiles.
    abstract fun userProfileDao(): UserProfileDao

    // Provides access to database operations for campaigns.
    abstract fun campaignDao(): CampaignDao

    // Provides access to database operations for campaign memberships.
    abstract fun campaignMembershipDao(): CampaignMembershipDao

    // Provides access to database operations for player characters.
    abstract fun characterDao(): CharacterDao

    // Provides access to database operations for character assignments within campaigns.
    abstract fun campaignCharacterDao(): CampaignCharacterDao

    // Provides access to database operations for campaign sessions.
    abstract fun sessionDao(): SessionDao

    // Provides access to database operations for session notes.
    abstract fun sessionNoteDao(): SessionNoteDao

    // Provides access to database operations for dice rolls.
    abstract fun diceRollDao(): DiceRollDao

    // Provides access to database operations for campaign photos.
    abstract fun campaignPhotoDao(): CampaignPhotoDao
}
