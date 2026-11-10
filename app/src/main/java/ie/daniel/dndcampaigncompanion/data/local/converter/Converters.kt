package ie.daniel.dndcampaigncompanion.data.local.converter

import androidx.room.TypeConverter
import ie.daniel.dndcampaigncompanion.data.local.entity.CampaignRole
import ie.daniel.dndcampaigncompanion.data.local.entity.CampaignStatus
import ie.daniel.dndcampaigncompanion.data.local.entity.SessionStatus

/**
 * Converts enum values into Strings so they can be stored in the Room database.
 *
 * Room does not store enum values directly, so these converters allow the
 * campaign roles, campaign statuses and session statuses to be saved as text.
 * When reading data back, the Strings are converted into their original enum values.
 */
class Converters {

    // Converts a CampaignStatus enum into its name so it can be stored as a String.
    // For example, CampaignStatus.ACTIVE becomes "ACTIVE".
    @TypeConverter
    fun fromCampaignStatus(value: CampaignStatus): String {
        return value.name
    }

    // Converts the stored String back into its matching CampaignStatus enum.
    // The String must match one of the names defined in CampaignStatus.
    @TypeConverter
    fun toCampaignStatus(value: String): CampaignStatus {
        return CampaignStatus.valueOf(value)
    }

    // Converts a CampaignRole enum into its name so it can be stored as a String.
    // For example, CampaignRole.DUNGEON_MASTER becomes "DUNGEON_MASTER".
    @TypeConverter
    fun fromCampaignRole(value: CampaignRole): String {
        return value.name
    }

    // Converts the stored String back into its matching CampaignRole enum.
    // The String must match one of the names defined in CampaignRole.
    @TypeConverter
    fun toCampaignRole(value: String): CampaignRole {
        return CampaignRole.valueOf(value)
    }

    // Converts a SessionStatus enum into its name so it can be stored as a String.
    // For example, SessionStatus.COMPLETED becomes "COMPLETED".
    @TypeConverter
    fun fromSessionStatus(value: SessionStatus): String {
        return value.name
    }

    // Converts the stored String back into its matching SessionStatus enum.
    // The String must match one of the names defined in SessionStatus.
    @TypeConverter
    fun toSessionStatus(value: String): SessionStatus {
        return SessionStatus.valueOf(value)
    }
}
