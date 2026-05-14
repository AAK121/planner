package com.planner.app.feature.analytics

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.planner.app.domain.model.*
import com.planner.app.domain.repository.ActivityRepository
import com.planner.app.domain.usecase.analytics.*
import com.planner.app.domain.usecase.log.GetLogsForActivityUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import javax.inject.Inject

data class AnalyticsUiState(
    val activity: Activity? = null,
    val analyticsData: AnalyticsData? = null,
    val isLoading: Boolean = true,
)

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val activityRepository: ActivityRepository,
    private val getLogsForActivity: GetLogsForActivityUseCase,
    private val computeStreak: ComputeStreakUseCase,
    private val computeCompliance: ComputeComplianceUseCase,
    private val computeHeatmap: ComputeHeatmapUseCase,
) : ViewModel() {

    private val activityId: String = checkNotNull(savedStateHandle["activityId"])

    val uiState: StateFlow<AnalyticsUiState> = getLogsForActivity(activityId)
        .combine(flowOf(activityRepository.getById(activityId))) { logs, activity ->
            if (activity == null) return@combine AnalyticsUiState(isLoading = false)
            val today = LocalDate.now()
            val streak = computeStreak(logs, today)
            val compliance = computeCompliance(activity, logs, today)
            val heatmap = computeHeatmap(logs)
            AnalyticsUiState(
                activity = activity,
                analyticsData = AnalyticsData(
                    activityId = activityId,
                    currentStreak = streak.current,
                    longestStreak = streak.longest,
                    complianceRate7d = compliance.rate7d,
                    complianceRate30d = compliance.rate30d,
                    complianceRate90d = compliance.rate90d,
                    complianceRateAll = compliance.rateAll,
                    totalDone = compliance.totalDone,
                    totalLogged = compliance.totalLogged,
                    heatmapCells = heatmap,
                    chartSeries = emptyList(),
                    missedDayPatterns = emptyList(),
                    averageDurationMinutes = if (logs.isEmpty()) 0
                                            else logs.sumOf { it.durationMinutes } / logs.size,
                ),
                isLoading = false,
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AnalyticsUiState())
}
