package com.planner.app.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.planner.app.data.local.datastore.PreferencesDataStore
import com.planner.app.domain.model.Activity
import com.planner.app.domain.model.ActivityType
import com.planner.app.domain.model.Schedule
import com.planner.app.domain.usecase.activity.CreateActivityUseCase
import com.planner.app.core.utils.newId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

data class PresetTemplate(
    val emoji: String,
    val name: String,
    val color: String,
    val defaultDays: List<Int>,
)

val defaultPresets = listOf(
    PresetTemplate("🏋️", "Gym", "#5B9E9E", listOf(1, 3, 5)),
    PresetTemplate("📖", "Reading", "#D4816B", listOf(1, 2, 3, 4, 5, 6, 7)),
    PresetTemplate("🏃", "Running", "#C9A24E", listOf(2, 4, 6)),
    PresetTemplate("🧘", "Meditation", "#7A9E7A", listOf(1, 2, 3, 4, 5, 6, 7)),
    PresetTemplate("💧", "Water intake", "#5B9E9E", listOf(1, 2, 3, 4, 5, 6, 7)),
    PresetTemplate("📝", "Journaling", "#D4816B", listOf(1, 2, 3, 4, 5, 6, 7)),
    PresetTemplate("💡", "Learn something", "#C9A24E", listOf(1, 2, 3, 4, 5)),
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val prefs: PreferencesDataStore,
    private val createActivity: CreateActivityUseCase,
) : ViewModel() {

    fun completeOnboarding(selectedPresets: List<PresetTemplate>) {
        viewModelScope.launch {
            selectedPresets.forEach { preset ->
                createActivity(
                    Activity(
                        id = newId(),
                        name = preset.name,
                        type = ActivityType.RECURRING,
                        color = preset.color,
                        emoji = preset.emoji,
                        schedule = Schedule(daysOfWeek = preset.defaultDays),
                        createdAt = Instant.now().toEpochMilli(),
                    )
                )
            }
            prefs.setOnboardingDone(true)
        }
    }
}
