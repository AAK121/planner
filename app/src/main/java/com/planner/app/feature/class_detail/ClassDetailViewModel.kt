package com.planner.app.feature.class_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.planner.app.domain.model.Activity
import com.planner.app.domain.model.HeatmapCell
import com.planner.app.domain.model.LogStatus
import com.planner.app.domain.model.PeriodCount
import com.planner.app.domain.repository.ActivityRepository
import com.planner.app.domain.repository.LogRepository
import com.planner.app.domain.usecase.analytics.ComputeActivityCountsUseCase
import com.planner.app.domain.usecase.analytics.ComputeHeatmapUseCase
import com.planner.app.domain.usecase.analytics.ComputeStreakUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import java.net.URLDecoder
import javax.inject.Inject

data class ClassDetailUiState(
    val className: String = "",
    val activities: List<ActivityRow> = emptyList(),
    val totalDone: Int = 0,
    val totalSkipped: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val periodCounts: List<PeriodCount> = emptyList(),
    val heatmapCells: List<HeatmapCell> = emptyList(),
    val isLoading: Boolean = true,
)

data class ActivityRow(
    val activity: Activity,
    val doneCount: Int,
    val skipCount: Int,
)

@HiltViewModel
class ClassDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val activityRepository: ActivityRepository,
    private val logRepository: LogRepository,
    private val computeStreak: ComputeStreakUseCase,
    private val computeCounts: ComputeActivityCountsUseCase,
    private val computeHeatmap: ComputeHeatmapUseCase,
) : ViewModel() {

    private val className: String = URLDecoder.decode(
        checkNotNull(savedStateHandle["className"]), "UTF-8",
    )

    val uiState: StateFlow<ClassDetailUiState> = combine(
        activityRepository.observeAll(),
        // 12-month window so totals are meaningful but bounded.
        logRepository.observeAllInRange(LocalDate.now().minusDays(365), LocalDate.now()),
    ) { activities, logs ->
        val inClass = activities.filter { it.className == className }
        val classLogs = logs.filter { log -> inClass.any { it.id == log.activityId } }

        val rows = inClass.map { activity ->
            val activityLogs = classLogs.filter { it.activityId == activity.id }
            ActivityRow(
                activity = activity,
                doneCount = activityLogs.count { it.status == LogStatus.DONE },
                skipCount = activityLogs.count { it.status == LogStatus.SKIPPED },
            )
        }

        val today = LocalDate.now()
        val streak = computeStreak(classLogs, today)
        val periodCounts = computeCounts(classLogs, today)
        val heatmap = computeHeatmap(classLogs)

        ClassDetailUiState(
            className = className,
            activities = rows,
            totalDone = rows.sumOf { it.doneCount },
            totalSkipped = rows.sumOf { it.skipCount },
            currentStreak = streak.current,
            longestStreak = streak.longest,
            periodCounts = periodCounts,
            heatmapCells = heatmap,
            isLoading = false,
        )
    }.flowOn(Dispatchers.Default)
     .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ClassDetailUiState())
}
