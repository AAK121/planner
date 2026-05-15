package com.planner.app.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.planner.app.domain.model.*
import com.planner.app.domain.repository.ActivityRepository
import com.planner.app.domain.repository.LogRepository
import com.planner.app.domain.usecase.analytics.ComputeCorrelationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import javax.inject.Inject

enum class StatsPeriod(val days: Long, val label: String) {
    DAYS_30(30, "30 days"),
    DAYS_90(90, "90 days"),
    DAYS_365(365, "1 year"),
}

data class AllocationItem(
    val id: String,            // activityId when isClass = false, className when isClass = true
    val name: String,
    val fraction: Float,
    val trackAnalytics: Boolean,
    val isClass: Boolean = false,
)

data class DashboardUiState(
    val momentumScore: Int = 0,
    val timeAllocations: List<AllocationItem> = emptyList(),
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

    private val _period = MutableStateFlow(StatsPeriod.DAYS_30)
    val period: StateFlow<StatsPeriod> = _period.asStateFlow()

    fun setPeriod(p: StatsPeriod) { _period.value = p }

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<DashboardUiState> = _period.flatMapLatest { period ->
        val today = LocalDate.now()
        val from = today.minusDays(period.days)
        combine(
            activityRepository.observeAll(),
            logRepository.observeAllInRange(from, today),
        ) { activities, logs ->
            val doneLogs = logs.filter { it.status == LogStatus.DONE }

            val totalDone = doneLogs.size.coerceAtLeast(1)
            // Per-activity done counts
            val countByActivity = activities.associate { activity ->
                activity.id to doneLogs.count { it.activityId == activity.id }
            }
            // Partition by whether the activity has a className
            val (classed, unclassed) = activities.partition { !it.className.isNullOrBlank() }
            // Aggregate classed activities by className
            val classGroups = classed
                .groupBy { it.className!! }
                .mapNotNull { (className, group) ->
                    val total = group.sumOf { countByActivity[it.id] ?: 0 }
                    if (total <= 0) null else AllocationItem(
                        id = className,
                        name = className,
                        fraction = total.toFloat() / totalDone,
                        // List the class as trackable if ANY underlying activity is trackable.
                        trackAnalytics = group.any { it.trackAnalytics },
                        isClass = true,
                    )
                }
            // Unclassed activities stay as individual slices
            val singles = unclassed.mapNotNull { activity ->
                val count = countByActivity[activity.id] ?: 0
                if (count <= 0) null else AllocationItem(
                    id = activity.id,
                    name = activity.name,
                    fraction = count.toFloat() / totalDone,
                    trackAnalytics = activity.trackAnalytics,
                    isClass = false,
                )
            }
            val allocations = (classGroups + singles).sortedByDescending { it.fraction }

            val correlations = computeCorrelations(activities, logs)

            val doneThis7 = doneLogs.count { it.date >= today.minusDays(7) }
            val donePrev7 = doneLogs.count { it.date < today.minusDays(7) && it.date >= today.minusDays(14) }
            val momentum = if (donePrev7 == 0) {
                (doneThis7 * 14).coerceAtMost(100)
            } else {
                ((doneThis7.toFloat() / donePrev7.toFloat()) * 50).toInt().coerceIn(0, 100)
            }

            DashboardUiState(
                momentumScore = momentum,
                timeAllocations = allocations,
                correlations = correlations,
                isLoading = false,
            )
        }
    }.flowOn(Dispatchers.Default)
     .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState())
}
