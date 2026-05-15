package com.planner.app.domain.usecase.analytics

import com.planner.app.domain.model.Activity
import com.planner.app.domain.model.ActivityCorrelation
import com.planner.app.domain.model.ActivityLog
import com.planner.app.domain.model.LogStatus
import java.time.LocalDate
import javax.inject.Inject
import kotlin.math.sqrt

class ComputeCorrelationsUseCase @Inject constructor() {
    operator fun invoke(
        activities: List<Activity>,
        logs: List<ActivityLog>,
        minSampleDays: Int = 14,
    ): List<ActivityCorrelation> {
        if (activities.size < 2) return emptyList()
        val doneByActivity = logs
            .filter { it.status == LogStatus.DONE }
            .groupBy { it.activityId }
            .mapValues { (_, v) -> v.map { it.date }.toSet() }
        if (doneByActivity.size < 2) return emptyList()

        val correlations = mutableListOf<ActivityCorrelation>()
        val ids = activities.map { it.id }

        for (i in ids.indices) {
            for (j in i + 1 until ids.size) {
                val daysA = doneByActivity[ids[i]] ?: continue
                val daysB = doneByActivity[ids[j]] ?: continue
                val allDays = (daysA + daysB).toSortedSet()
                if (allDays.size < minSampleDays) continue

                val score = pearson(allDays.toList(), daysA, daysB)
                if (score > 0.3f) {
                    val pct = (score * 100).toInt()
                    val nameA = activities.first { it.id == ids[i] }.name
                    val nameB = activities.first { it.id == ids[j] }.name
                    correlations.add(
                        ActivityCorrelation(
                            activityIdA = ids[i],
                            activityIdB = ids[j],
                            correlationScore = score,
                            description = "$nameA is ${pct}% more consistent on $nameB days",
                        )
                    )
                }
            }
        }
        return correlations.sortedByDescending { it.correlationScore }
    }

    private fun pearson(days: List<LocalDate>, setA: Set<LocalDate>, setB: Set<LocalDate>): Float {
        val n = days.size.toFloat()
        val xs = days.map { if (it in setA) 1f else 0f }
        val ys = days.map { if (it in setB) 1f else 0f }
        val meanX = xs.sum() / n
        val meanY = ys.sum() / n
        val num = xs.zip(ys).sumOf { (x, y) -> ((x - meanX) * (y - meanY)).toDouble() }
        val denX = sqrt(xs.sumOf { ((it - meanX) * (it - meanX)).toDouble() })
        val denY = sqrt(ys.sumOf { ((it - meanY) * (it - meanY)).toDouble() })
        if (denX == 0.0 || denY == 0.0) return 0f
        return (num / (denX * denY)).toFloat().coerceIn(-1f, 1f)
    }
}
