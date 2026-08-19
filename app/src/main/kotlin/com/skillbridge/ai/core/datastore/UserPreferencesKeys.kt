package com.skillbridge.ai.core.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

/**
 * Centralised registry of all [androidx.datastore.preferences.core.Preferences.Key] constants
 * used by the Preferences DataStore.
 *
 * Adding a new preference:
 * 1. Declare its key here.
 * 2. Add a typed accessor in the relevant DataStore repository.
 */
object UserPreferencesKeys {

    // ── Authentication ─────────────────────────────────────────────────────────

    /** Whether the user is currently logged in. */
    val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")

    /** JWT / Bearer token for authenticated API calls. */
    val AUTH_TOKEN = stringPreferencesKey("auth_token")

    // ── Appearance ─────────────────────────────────────────────────────────────

    /**
     * User's preferred theme.
     * Expected values: "light", "dark", "system".
     */
    val THEME_PREFERENCE = stringPreferencesKey("theme_preference")

    // ── Onboarding ─────────────────────────────────────────────────────────────

    /** Whether the user has completed the onboarding flow. */
    val HAS_COMPLETED_ONBOARDING = booleanPreferencesKey("has_completed_onboarding")

    // ── User Profile ───────────────────────────────────────────────────────────

    /** Locally cached user ID for quick look-ups. */
    val USER_ID = stringPreferencesKey("user_id")

    /** Locally cached display name. */
    val USER_DISPLAY_NAME = stringPreferencesKey("user_display_name")
}
