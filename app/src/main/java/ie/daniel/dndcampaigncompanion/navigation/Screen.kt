package ie.daniel.dndcampaigncompanion.navigation

/**
 * Stores the route names used by the application's navigation system.
 *
 * Keeping route names in one place helps avoid spelling mistakes and
 * makes it easier to maintain navigation between screens.
 */
object Screen {

    // Profile navigation routes.
    const val PROFILE_SELECTION = "profile_selection"
    const val PROFILE_CREATION = "profile_creation"
    const val PROFILE_EDIT = "profile_edit/{profileId}"
    const val PROFILE_MANAGEMENT = "profile_management"

    // Builds the profile edit route using the ID of the profile being edited.
    // The {profileId} placeholder in PROFILE_EDIT is replaced with the actual ID.
    fun profileEdit(profileId: String): String {
        return "profile_edit/$profileId"
    }

    // Campaign navigation routes.
    const val CAMPAIGNS = "campaigns"
    const val CAMPAIGN_CREATION = "campaign_creation"
    const val CAMPAIGN_DETAILS = "campaign_details/{campaignId}"
    const val CAMPAIGN_EDIT = "campaign_edit/{campaignId}"
    const val CAMPAIGN_MEMBER_MANAGEMENT =
        "campaign_member_management/{campaignId}"
    const val CAMPAIGN_CHARACTER_ASSIGNMENT =
        "campaign_character_assignment/{campaignId}"

    // Builds the campaign details route for a specific campaign.
    fun campaignDetails(campaignId: String): String {
        return "campaign_details/$campaignId"
    }

    // Builds the campaign edit route for a specific campaign.
    fun campaignEdit(campaignId: String): String {
        return "campaign_edit/$campaignId"
    }

    // Builds the campaign member management route for a specific campaign.
    fun campaignMemberManagement(campaignId: String): String {
        return "campaign_member_management/$campaignId"
    }

    // Builds the character assignment route for a specific campaign.
    fun campaignCharacterAssignment(campaignId: String): String {
        return "campaign_character_assignment/$campaignId"
    }

    // Character navigation routes.
    const val CHARACTERS = "characters"
    const val CHARACTER_CREATION = "character_creation"
    const val CHARACTER_DETAILS = "character_details/{characterId}"
    const val CHARACTER_EDIT = "character_edit/{characterId}"

    // Builds the character details route for a specific character.
    fun characterDetails(characterId: String): String {
        return "character_details/$characterId"
    }

    // Builds the character edit route for a specific character.
    fun characterEdit(characterId: String): String {
        return "character_edit/$characterId"
    }

    // Session navigation routes.
    const val SESSIONS = "sessions"
    const val SESSION_DETAILS = "session_details"

    // Dice roller and campaign gallery routes.
    const val DICE_ROLLER = "dice_roller"
    const val CAMPAIGN_GALLERY = "campaign_gallery"
}
