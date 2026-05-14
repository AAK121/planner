package com.planner.app.feature.create_activity

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.planner.app.domain.model.*
import com.planner.app.domain.usecase.activity.CreateActivityUseCase
import com.planner.app.domain.usecase.activity.UpdateActivityUseCase
import com.planner.app.domain.repository.ActivityRepository
import com.planner.app.core.utils.newId
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
    val selectedDays: List<Int> = emptyList(),
    val times: List<String> = emptyList(),
    val hasReminder: Boolean = false,
    val variableTree: List<VariableNode> = emptyList(),
    val goal: Goal? = null,
    val currentStep: Int = 0,
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val savedSuccessfully: Boolean = false,
)

@HiltViewModel
class CreateActivityViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val activityRepository: ActivityRepository,
    private val createActivity: CreateActivityUseCase,
    private val updateActivity: UpdateActivityUseCase,
) : ViewModel() {

    private val editId: String? = savedStateHandle["activityId"]

    private val _state = MutableStateFlow(CreateActivityUiState())
    val state: StateFlow<CreateActivityUiState> = _state

    init {
        editId?.takeIf { it.isNotBlank() }?.let { id ->
            viewModelScope.launch {
                activityRepository.getById(id)?.let { activity ->
                    _state.value = CreateActivityUiState(
                        name = activity.name,
                        type = activity.type,
                        color = activity.color,
                        emoji = activity.emoji,
                        selectedDays = activity.schedule.daysOfWeek,
                        times = activity.schedule.timesOfDay,
                        hasReminder = activity.schedule.hasReminder,
                        variableTree = activity.variableTree,
                        goal = activity.goal,
                        isEditing = true,
                    )
                }
            }
        }
    }

    fun setName(name: String)                { _state.value = _state.value.copy(name = name) }
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
    fun setVariableTree(tree: List<VariableNode>) { _state.value = _state.value.copy(variableTree = tree) }
    fun setGoal(goal: Goal?)                 { _state.value = _state.value.copy(goal = goal) }
    fun nextStep()                           { _state.value = _state.value.copy(currentStep = _state.value.currentStep + 1) }
    fun prevStep()                           { _state.value = _state.value.copy(currentStep = (_state.value.currentStep - 1).coerceAtLeast(0)) }

    fun save() {
        val s = _state.value
        if (s.name.isBlank()) return
        _state.value = s.copy(isSaving = true)
        viewModelScope.launch {
            val activity = Activity(
                id = editId?.takeIf { it.isNotBlank() } ?: newId(),
                name = s.name,
                type = s.type,
                color = s.color,
                emoji = s.emoji,
                schedule = Schedule(
                    daysOfWeek = s.selectedDays,
                    timesOfDay = s.times,
                    hasReminder = s.hasReminder,
                ),
                variableTree = s.variableTree,
                goal = s.goal,
                createdAt = Instant.now().toEpochMilli(),
            )
            if (s.isEditing) updateActivity(activity) else createActivity(activity)
            _state.value = _state.value.copy(isSaving = false, savedSuccessfully = true)
        }
    }
}
