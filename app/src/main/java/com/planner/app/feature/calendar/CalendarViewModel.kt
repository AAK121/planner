package com.planner.app.feature.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.planner.app.domain.model.Activity
import com.planner.app.domain.model.ActivityLog
import com.planner.app.domain.model.LogStatus
import com.planner.app.domain.repository.ActivityRepository
import com.planner.app.domain.repository.LogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

data class CalendarUiState(
    val currentMonth: YearMonth = YearMonth.now(),
    val selectedDate: LocalDate = LocalDate.now(),
    val logsByDate: Map<LocalDate, List<ActivityLog>> = emptyMap(),
    val activitiesForSelected: List<Activity> = emptyList(),
    val logsForSelected: List<ActivityLog> = emptyList(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val activityRepository: ActivityRepository,
    private val logRepository: LogRepository,
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    private val _currentMonth = MutableStateFlow(YearMonth.now())

    val uiState: StateFlow<CalendarUiState> = combine(
        _currentMonth,
        _selectedDate,
        activityRepository.observeAll(),
    ) { month, selected, activities ->
        val from = month.atDay(1)
        val to   = month.atEndOfMonth()
        val logs = logRepository.getAllInRange(from, to)
        val logsByDate = logs.groupBy { it.date }
        val logsForSelected = logsByDate[selected] ?: emptyList()
        val activityIds = logsForSelected.map { it.activityId }.toSet()
        CalendarUiState(
            currentMonth = month,
            selectedDate = selected,
            logsByDate = logsByDate,
            activitiesForSelected = activities.filter { it.id in activityIds },
            logsForSelected = logsForSelected,
            isLoading = false,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CalendarUiState())

    fun selectDate(date: LocalDate) { _selectedDate.value = date }
    fun prevMonth() { _currentMonth.value = _currentMonth.value.minusMonths(1) }
    fun nextMonth() { _currentMonth.value = _currentMonth.value.plusMonths(1) }
}
