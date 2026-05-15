package com.planner.app.feature.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.planner.app.domain.model.Activity
import com.planner.app.domain.model.ActivityLog
import com.planner.app.domain.model.ActivityType
import com.planner.app.domain.repository.ActivityRepository
import com.planner.app.domain.repository.LogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

data class CalendarUiState(
    val currentMonth: YearMonth = YearMonth.now(),
    val selectedDate: LocalDate = LocalDate.now(),
    val logsByDate: Map<LocalDate, List<ActivityLog>> = emptyMap(),
    val scheduledByDate: Map<LocalDate, List<Activity>> = emptyMap(),
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

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<CalendarUiState> = _currentMonth.flatMapLatest { month ->
        val from = month.atDay(1)
        val to   = month.atEndOfMonth()
        combine(
            _selectedDate,
            activityRepository.observeAll(),
            logRepository.observeAllInRange(from, to),
        ) { selected, activities, logs ->
            val logsByDate = logs.groupBy { it.date }

            val scheduledByDate = mutableMapOf<LocalDate, List<Activity>>()
            var day = from
            while (!day.isAfter(to)) {
                val scheduled = activities.filter { it.isScheduledFor(day) }
                if (scheduled.isNotEmpty()) scheduledByDate[day] = scheduled
                day = day.plusDays(1)
            }

            val activitiesForSelected = activities.filter { it.isScheduledFor(selected) }

            CalendarUiState(
                currentMonth = month,
                selectedDate = selected,
                logsByDate = logsByDate,
                scheduledByDate = scheduledByDate,
                activitiesForSelected = activitiesForSelected,
                logsForSelected = logsByDate[selected] ?: emptyList(),
                isLoading = false,
            )
        }
    }.flowOn(Dispatchers.Default)
     .stateIn(viewModelScope, SharingStarted.Eagerly, CalendarUiState())

    fun selectDate(date: LocalDate) { _selectedDate.value = date }
    fun prevMonth() { _currentMonth.value = _currentMonth.value.minusMonths(1) }
    fun nextMonth() { _currentMonth.value = _currentMonth.value.plusMonths(1) }

    private fun Activity.isScheduledFor(date: LocalDate): Boolean = when (type) {
        ActivityType.RECURRING -> schedule.daysOfWeek.contains(date.dayOfWeek.value)
        ActivityType.OCCASIONAL -> schedule.daysOfWeek.contains(date.dayOfWeek.value)
        ActivityType.ONE_OFF -> schedule.dueDate?.let {
            java.time.Instant.ofEpochMilli(it)
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate() == date
        } ?: false
    }
}
