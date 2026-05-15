package com.planner.app.feature.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.planner.app.core.theme.ThemeVariant
import com.planner.app.data.local.datastore.PreferencesDataStore
import com.planner.app.notifications.NotificationHelper
import com.planner.app.notifications.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val themeVariant: ThemeVariant = ThemeVariant.LIGHT,
    val notifReminders: Boolean = false,
    val notifStreaks: Boolean = false,
    val notifInsights: Boolean = false,
    val notifCelebrations: Boolean = false,
    val notifDailyPlan: Boolean = false,
    val llmOnDeviceEnabled: Boolean = false,
    val llmCloudEnabled: Boolean = false,
    val llmProvider: String = "anthropic",
    val llmApiKey: String = "",
    val username: String = "",
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: PreferencesDataStore,
    private val reminderScheduler: ReminderScheduler,
    @ApplicationContext private val appContext: Context,
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        combine(
            prefs.themeVariant,
            prefs.notifReminders,
            prefs.notifStreaks,
            prefs.notifInsights,
            prefs.notifCelebrations,
            prefs.notifDailyPlan,
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
                notifDailyPlan       = values[5] as Boolean,
                llmOnDeviceEnabled   = values[6] as Boolean,
                llmCloudEnabled      = values[7] as Boolean,
                llmProvider          = values[8] as String,
                llmApiKey            = values[9] as String,
            )
        },
        prefs.username,
    ) { settings, username ->
        settings.copy(username = username)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun setTheme(v: ThemeVariant) = launch { prefs.setThemeVariant(v) }
    fun setNotifReminders(v: Boolean) = launch {
        prefs.setNotifReminders(v)
        if (v) reminderScheduler.scheduleAllForToday()
        else   reminderScheduler.cancelAll()
    }
    fun setNotifStreaks(v: Boolean) = launch { prefs.setNotifStreaks(v) }
    fun setNotifInsights(v: Boolean) = launch { prefs.setNotifInsights(v) }
    fun setNotifCelebrations(v: Boolean) = launch { prefs.setNotifCelebrations(v) }
    fun setNotifDailyPlan(v: Boolean) = launch { prefs.setNotifDailyPlan(v) }
    fun setLlmOnDevice(v: Boolean) = launch { prefs.setLlmOnDeviceEnabled(v) }
    fun setLlmCloud(v: Boolean) = launch { prefs.setLlmCloudEnabled(v) }
    fun setLlmProvider(v: String) = launch { prefs.setLlmProvider(v) }
    fun setLlmApiKey(v: String) = launch { prefs.setLlmApiKey(v) }
    fun setUsername(v: String) = launch { prefs.setUsername(v) }

    /** Fires a synthetic notification right now. Bypasses every gate — used to
     *  verify that the system permission, channel and helper are working. */
    fun sendTestNotification() {
        NotificationHelper.showActivityReminder(
            context = appContext,
            activityId = "test",
            activityName = "Test reminder",
            className = null,
            minutesUntil = 0,
            notifId = 99_999,
        )
    }

    private fun launch(block: suspend () -> Unit) { viewModelScope.launch { block() } }
}
