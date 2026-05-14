package com.planner.app.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.planner.app.core.theme.ThemeVariant
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "planner_prefs")

@Singleton
class PreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private object Keys {
        val THEME_VARIANT        = stringPreferencesKey("theme_variant")
        val ONBOARDING_DONE      = booleanPreferencesKey("onboarding_done")
        val LLM_ONDEVICE_ENABLED = booleanPreferencesKey("llm_ondevice_enabled")
        val LLM_CLOUD_ENABLED    = booleanPreferencesKey("llm_cloud_enabled")
        val LLM_PROVIDER         = stringPreferencesKey("llm_provider")
        val LLM_API_KEY          = stringPreferencesKey("llm_api_key")
        val NOTIF_REMINDERS      = booleanPreferencesKey("notif_reminders")
        val NOTIF_STREAKS        = booleanPreferencesKey("notif_streaks")
        val NOTIF_INSIGHTS       = booleanPreferencesKey("notif_insights")
        val NOTIF_CELEBRATIONS   = booleanPreferencesKey("notif_celebrations")
        val USERNAME             = stringPreferencesKey("username")
    }

    val themeVariant: Flow<ThemeVariant> = context.dataStore.data.map { prefs ->
        ThemeVariant.valueOf(prefs[Keys.THEME_VARIANT] ?: ThemeVariant.LIGHT.name)
    }

    val onboardingDone: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.ONBOARDING_DONE] ?: false
    }

    val llmOnDeviceEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.LLM_ONDEVICE_ENABLED] ?: false
    }

    val llmCloudEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.LLM_CLOUD_ENABLED] ?: false
    }

    val llmProvider: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.LLM_PROVIDER] ?: "anthropic"
    }

    val llmApiKey: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.LLM_API_KEY] ?: ""
    }

    val notifReminders: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.NOTIF_REMINDERS] ?: true
    }

    val notifStreaks: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.NOTIF_STREAKS] ?: true
    }

    val notifInsights: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.NOTIF_INSIGHTS] ?: true
    }

    val notifCelebrations: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.NOTIF_CELEBRATIONS] ?: true
    }

    val username: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.USERNAME] ?: ""
    }

    suspend fun setThemeVariant(variant: ThemeVariant) {
        context.dataStore.edit { it[Keys.THEME_VARIANT] = variant.name }
    }

    suspend fun setOnboardingDone(done: Boolean) {
        context.dataStore.edit { it[Keys.ONBOARDING_DONE] = done }
    }

    suspend fun setLlmOnDeviceEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.LLM_ONDEVICE_ENABLED] = enabled }
    }

    suspend fun setLlmCloudEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.LLM_CLOUD_ENABLED] = enabled }
    }

    suspend fun setLlmProvider(provider: String) {
        context.dataStore.edit { it[Keys.LLM_PROVIDER] = provider }
    }

    suspend fun setLlmApiKey(key: String) {
        context.dataStore.edit { it[Keys.LLM_API_KEY] = key }
    }

    suspend fun setNotifReminders(enabled: Boolean) {
        context.dataStore.edit { it[Keys.NOTIF_REMINDERS] = enabled }
    }

    suspend fun setNotifStreaks(enabled: Boolean) {
        context.dataStore.edit { it[Keys.NOTIF_STREAKS] = enabled }
    }

    suspend fun setNotifInsights(enabled: Boolean) {
        context.dataStore.edit { it[Keys.NOTIF_INSIGHTS] = enabled }
    }

    suspend fun setNotifCelebrations(enabled: Boolean) {
        context.dataStore.edit { it[Keys.NOTIF_CELEBRATIONS] = enabled }
    }

    suspend fun setUsername(name: String) {
        context.dataStore.edit { it[Keys.USERNAME] = name }
    }
}
