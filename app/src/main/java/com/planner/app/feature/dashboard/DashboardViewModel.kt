package com.planner.app.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.planner.app.domain.model.*
import com.planner.app.domain.repository.ActivityRepository
import com.planner.app.domain.repository.LogRepository
import com.planner.app.domain.usecase.analytics.ComputeCorrelationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import javax.inject.Inject

data class DashboardUiState(
    val momentumScore: Int = 0,
    val timeAllocations: List<Pair<String, Float>> = emptyList(),
    val correlations: List<ActivityCorrelation> = emptyList(),
    val weeklyInsight: String? = null,
    val isLoading: Boolean = true,
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val activityRepository: ActivityRepository,
    private val logRepository: LogRepository,
    private val computeCorrelations: ComputeCorrelationsUseCase,
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = activityRepository.observeAll()
        .flatMapLatest { activities ->
            val today = LocalDate.now()
            val from = today.minusDays(30)
            flowOf(logRepository.getAllInRange(from, today)).map { logs ->
                val totalDuration = logs.sumOf { it.durationMinutes }.toFloat().coerceAtLeast(1f)
                val allocations = activities.mapNotNull { activity ->
                    val actLogs = logs.filter { it.activityId == activity.id }
                    val duration = actLogs.sumOf { it.durationMinutes }.toFloat()
                    if (duration > 0) activity.name to (duration / totalDuration) else null
                }
                val correlations = computeCorrelations(activities, logs)
                val momentum = run {
                    val doneThis7 = logs.count { it.date >= today.minusDays(7) && it.status == LogStatus.DONE }
                    val donePrev7 = logs.count { it.date < today.minusDays(7) && it.date >= today.minusDays(14) && it.status == LogStatus.DONE }
                    if (donePrev7 == 0) 50
                    else ((doneThis7.toFloat() / donePrev7.toFloat()) * 50).toInt().coerceIn(0, 100)
                }
                DashboardUiState(
                    momentumScore = momentum,
                    timeAllocations = allocations,
                    correlations = correlations,
                    isLoading = false,
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState())
}
