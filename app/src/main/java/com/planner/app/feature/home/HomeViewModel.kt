package com.planner.app.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.planner.app.data.local.datastore.PreferencesDataStore
import com.planner.app.domain.model.Activity
import com.planner.app.domain.model.ActivityLog
import com.planner.app.domain.model.LogStatus
import com.planner.app.domain.repository.LogRepository
import com.planner.app.domain.usecase.activity.GetTodayActivitiesUseCase
import com.planner.app.domain.usecase.analytics.ComputeStreakUseCase
import com.planner.app.domain.usecase.log.LogActivityUseCase
import com.planner.app.core.utils.newId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject

data class HomeUiState(
    val activities: List<Activity> = emptyList(),
    val logs: Map<String, ActivityLog> = emptyMap(),
    val streaks: Map<String, Int> = emptyMap(),
    val progressFraction: Float = 0f,
    val doneCount: Int = 0,
    val totalCount: Int = 0,
    val today: LocalDate = LocalDate.now(),
    val isLoading: Boolean = true,
    val username: String = "",
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getTodayActivities: GetTodayActivitiesUseCase,
    private val logRepository: LogRepository,
    private val logActivity: LogActivityUseCase,
    private val computeStreak: ComputeStreakUseCase,
    private val prefs: PreferencesDataStore,
) : ViewModel() {

    private val today = LocalDate.now()

    val uiState: StateFlow<HomeUiState> = combine(
        getTodayActivities(today).flatMapLatest { activities ->
            logRepository.observeForDay(today).map { todayLogs ->
                val logsByActivity = todayLogs.associateBy { it.activityId }
                val doneCount = logsByActivity.values.count { it.status == LogStatus.DONE }
                // Activities whose log is PENDING or absent are still "pending"
                HomeUiState(
                    activities = activities,
                    logs = logsByActivity,
                    streaks = activities.associate { it.id to 0 },
                    progressFraction = if (activities.isEmpty()) 0f
                                       else doneCount.toFloat() / activities.size,
                    doneCount = doneCount,
                    totalCount = activities.size,
                    today = today,
                    isLoading = false,
                )
            }
        },
        prefs.username,
    ) { baseState, username ->
        baseState.copy(username = username)
    }.flowOn(Dispatchers.Default)
     .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun toggleDone(activity: Activity) {
        viewModelScope.launch {
            val existing = logRepository.getForActivityAndDay(activity.id, today)
            if (existing != null) {
                val newStatus = if (existing.status == LogStatus.DONE) LogStatus.PENDING else LogStatus.DONE
                logActivity(existing.copy(status = newStatus))
            } else {
                logActivity(
                    ActivityLog(
                        id = newId(),
                        activityId = activity.id,
                        date = today,
                        status = LogStatus.DONE,
                        durationMinutes = 0,
                        createdAt = Instant.now().toEpochMilli(),
                    )
                )
            }
            // Widget updates itself reactively via Room flow + collectAsState;
            // no manual refresh hook needed.
        }
    }
}
