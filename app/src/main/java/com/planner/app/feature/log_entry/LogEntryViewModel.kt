package com.planner.app.feature.log_entry

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.planner.app.domain.model.Activity
import com.planner.app.domain.model.ActivityLog
import com.planner.app.domain.model.LogStatus
import com.planner.app.domain.repository.ActivityRepository
import com.planner.app.domain.repository.LogRepository
import com.planner.app.domain.usecase.log.LogActivityUseCase
import com.planner.app.core.utils.newId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject

data class LogEntryUiState(
    val activity: Activity? = null,
    val status: LogStatus = LogStatus.DONE,
    val durationMinutes: Int = 30,
    val data: Map<String, String> = emptyMap(),
    val note: String = "",
    val isSaving: Boolean = false,
    val savedSuccessfully: Boolean = false,
    val existingLogId: String? = null,
)

@HiltViewModel
class LogEntryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val activityRepository: ActivityRepository,
    private val logRepository: LogRepository,
    private val logActivity: LogActivityUseCase,
) : ViewModel() {

    private val activityId: String = checkNotNull(savedStateHandle["activityId"])

    private val _state = MutableStateFlow(LogEntryUiState())
    val state: StateFlow<LogEntryUiState> = _state

    init {
        viewModelScope.launch {
            val activity = activityRepository.getById(activityId)
            val existing = logRepository.getForActivityAndDay(activityId, LocalDate.now())
            _state.value = _state.value.copy(
                activity = activity,
                status = existing?.status ?: LogStatus.PENDING,
                durationMinutes = existing?.durationMinutes ?: 30,
                data = existing?.data ?: emptyMap(),
                note = existing?.note ?: "",
                existingLogId = existing?.id,
            )
        }
    }

    fun setStatus(status: LogStatus) = _state.update { it.copy(status = status) }
    fun setDuration(minutes: Int)    = _state.update { it.copy(durationMinutes = minutes) }
    fun setDataValue(key: String, value: String) = _state.update { it.copy(data = it.data + (key to value)) }
    fun setNote(note: String)        = _state.update { it.copy(note = note) }

    fun save() {
        val s = _state.value
        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            logActivity(
                ActivityLog(
                    id = s.existingLogId ?: newId(),
                    activityId = activityId,
                    date = LocalDate.now(),
                    status = s.status,
                    durationMinutes = s.durationMinutes,
                    data = s.data,
                    note = s.note,
                    createdAt = Instant.now().toEpochMilli(),
                )
            )
            _state.update { it.copy(isSaving = false, savedSuccessfully = true) }
        }
    }
}
