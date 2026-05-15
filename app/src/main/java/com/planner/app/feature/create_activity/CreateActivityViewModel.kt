package com.planner.app.feature.create_activity

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.planner.app.domain.model.*
import com.planner.app.domain.usecase.activity.CreateActivityUseCase
import com.planner.app.domain.usecase.activity.UpdateActivityUseCase
import com.planner.app.domain.repository.ActivityRepository
import com.planner.app.core.utils.newId
import com.planner.app.notifications.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

data class CreateActivityUiState(
    val name: String = "",
    val type: ActivityType = ActivityType.RECURRING,
    val color: String = "#5B9E9E",
    val emoji: String = "📋",
    val selectedDays: List<Int> = listOf(1, 2, 3, 4, 5, 6, 7),  // Mon–Sun selected by default
    val times: List<String> = emptyList(),
    val dueDate: Long? = null,           // epoch millis, ONE_OFF only
    val hasReminder: Boolean = false,
    val variableTree: List<VariableNode> = emptyList(),
    val goal: Goal? = null,
    val trackAnalytics: Boolean = true,
    val className: String = "",
    val currentStep: Int = 0,
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val savedSuccessfully: Boolean = false,
    val nameError: String? = null,        // non-null = blocks Continue/Save
)

@HiltViewModel
class CreateActivityViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val activityRepository: ActivityRepository,
    private val createActivity: CreateActivityUseCase,
    private val updateActivity: UpdateActivityUseCase,
    private val reminderScheduler: ReminderScheduler,
) : ViewModel() {

    private val editId: String? = savedStateHandle.get<String>("activityId")
        ?.takeIf { it.isNotBlank() }

    private val _state = MutableStateFlow(CreateActivityUiState())
    val state: StateFlow<CreateActivityUiState> = _state

    // Cached snapshot of all existing activities, kept live via observeAll().
    // Used to validate name uniqueness case-insensitively against everything
    // except the activity being edited.
    private var existingActivities: List<Activity> = emptyList()

    init {
        editId?.let { id ->
            viewModelScope.launch {
                activityRepository.getById(id)?.let { activity ->
                    _state.value = CreateActivityUiState(
                        name = activity.name,
                        type = activity.type,
                        color = activity.color,
                        emoji = activity.emoji,
                        selectedDays = activity.schedule.daysOfWeek,
                        times = activity.schedule.timesOfDay,
                        dueDate = activity.schedule.dueDate,
                        hasReminder = activity.schedule.hasReminder,
                        variableTree = activity.variableTree,
                        goal = activity.goal,
                        trackAnalytics = activity.trackAnalytics,
                        className = activity.className.orEmpty(),
                        isEditing = true,
                    )
                    validateName()
                }
            }
        }
        viewModelScope.launch {
            activityRepository.observeAll().collect { all ->
                existingActivities = all
                validateName()
            }
        }
    }

    fun setName(name: String) {
        _state.value = _state.value.copy(name = name)
        validateName()
    }

    private fun validateName() {
        val trimmed = _state.value.name.trim()
        val duplicate = trimmed.isNotEmpty() && existingActivities.any { other ->
            other.id != editId && other.name.trim().equals(trimmed, ignoreCase = true)
        }
        _state.value = _state.value.copy(
            nameError = if (duplicate) "Name already taken" else null
        )
    }

    fun setType(type: ActivityType)          { _state.value = _state.value.copy(type = type) }
    fun setColor(color: String)              { _state.value = _state.value.copy(color = color) }
    fun setEmoji(emoji: String)              { _state.value = _state.value.copy(emoji = emoji) }
    fun toggleDay(day: Int) {
        val days = _state.value.selectedDays.toMutableList()
        if (days.contains(day)) days.remove(day) else days.add(day)
        _state.value = _state.value.copy(selectedDays = days.sorted())
    }
    fun addTime(time: String)                { _state.value = _state.value.copy(times = _state.value.times + time) }
    fun removeTime(time: String)             { _state.value = _state.value.copy(times = _state.value.times - time) }
    fun setHasReminder(v: Boolean)           { _state.value = _state.value.copy(hasReminder = v) }
    fun setDueDate(v: Long?)                 { _state.value = _state.value.copy(dueDate = v) }
    fun setVariableTree(tree: List<VariableNode>) { _state.value = _state.value.copy(variableTree = tree) }
    fun setGoal(goal: Goal?)                 { _state.value = _state.value.copy(goal = goal) }
    fun setTrackAnalytics(v: Boolean)        { _state.value = _state.value.copy(trackAnalytics = v) }
    fun setClassName(v: String)              { _state.value = _state.value.copy(className = v) }
    fun nextStep()                           { _state.value = _state.value.copy(currentStep = _state.value.currentStep + 1) }
    fun prevStep()                           { _state.value = _state.value.copy(currentStep = (_state.value.currentStep - 1).coerceAtLeast(0)) }
    fun goToStep(step: Int)                  { _state.value = _state.value.copy(currentStep = step.coerceIn(0, 3)) }

    fun save() {
        val s = _state.value
        if (s.name.isBlank()) return
        if (s.nameError != null) return   // safety net — UI also blocks Continue
        _state.value = s.copy(isSaving = true)
        viewModelScope.launch {
            val activity = Activity(
                id = editId ?: newId(),
                name = s.name,
                type = s.type,
                color = s.color,
                emoji = s.emoji,
                schedule = Schedule(
                    // Empty = invisible everywhere; fall back to every day so a half-filled
                    // schedule step still produces a usable activity.
                    daysOfWeek = s.selectedDays.ifEmpty { listOf(1, 2, 3, 4, 5, 6, 7) },
                    timesOfDay = s.times,
                    hasReminder = s.hasReminder,
                    dueDate = if (s.type == ActivityType.ONE_OFF) s.dueDate else null,
                ),
                variableTree = s.variableTree,
                goal = s.goal,
                trackAnalytics = s.trackAnalytics,
                className = s.className.trim().takeIf { it.isNotEmpty() },
                createdAt = Instant.now().toEpochMilli(),
            )
            if (s.isEditing) updateActivity(activity) else createActivity(activity)
            // Schedule alarms immediately — no WorkManager hop.
            if (activity.schedule.timesOfDay.isNotEmpty()) {
                reminderScheduler.scheduleAllForToday()
            }
            // Widget updates itself reactively via Room flow + collectAsState.
            _state.value = _state.value.copy(isSaving = false, savedSuccessfully = true)
        }
    }
}
