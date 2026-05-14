package com.planner.app.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.planner.app.core.theme.ThemeVariant
import com.planner.app.data.local.datastore.PreferencesDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val themeVariant: ThemeVariant = ThemeVariant.LIGHT,
    val notifReminders: Boolean = true,
    val notifStreaks: Boolean = true,
    val notifInsights: Boolean = true,
    val notifCelebrations: Boolean = true,
    val llmOnDeviceEnabled: Boolean = false,
    val llmCloudEnabled: Boolean = false,
    val llmProvider: String = "anthropic",
    val llmApiKey: String = "",
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: PreferencesDataStore,
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        prefs.themeVariant,
        prefs.notifReminders,
        prefs.notifStreaks,
        prefs.notifInsights,
        prefs.notifCelebrations,
        prefs.llmOnDeviceEnabled,
        prefs.llmCloudEnabled,
        prefs.llmProvider,
        prefs.llmApiKey,
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        SettingsUiState(
            themeVariant         = values[0] as ThemeVariant,
            notifReminders       = values[1] as Boolean,
            notifStreaks         = values[2] as Boolean,
            notifInsights        = values[3] as Boolean,
            notifCelebrations    = values[4] as Boolean,
            llmOnDeviceEnabled   = values[5] as Boolean,
            llmCloudEnabled      = values[6] as Boolean,
            llmProvider          = values[7] as String,
            llmApiKey            = values[8] as String,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun setTheme(v: ThemeVariant) = launch { prefs.setThemeVariant(v) }
    fun setNotifReminders(v: Boolean) = launch { prefs.setNotifReminders(v) }
    fun setNotifStreaks(v: Boolean) = launch { prefs.setNotifStreaks(v) }
    fun setNotifInsights(v: Boolean) = launch { prefs.setNotifInsights(v) }
    fun setNotifCelebrations(v: Boolean) = launch { prefs.setNotifCelebrations(v) }
    fun setLlmOnDevice(v: Boolean) = launch { prefs.setLlmOnDeviceEnabled(v) }
    fun setLlmCloud(v: Boolean) = launch { prefs.setLlmCloudEnabled(v) }
    fun setLlmProvider(v: String) = launch { prefs.setLlmProvider(v) }
    fun setLlmApiKey(v: String) = launch { prefs.setLlmApiKey(v) }

    private fun launch(block: suspend () -> Unit) = viewModelScope.launch { block() }
}
