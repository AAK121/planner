package com.planner.app.domain.usecase.analytics

import com.planner.app.domain.model.ActivityLog
import com.planner.app.domain.model.HeatmapCell
import com.planner.app.domain.model.LogStatus
import java.time.LocalDate
import javax.inject.Inject

class ComputeHeatmapUseCase @Inject constructor() {
    operator fun invoke(
        logs: List<ActivityLog>,
        weeksBack: Int = 12,
        today: LocalDate = LocalDate.now(),
    ): List<HeatmapCell> {
        val from = today.minusWeeks(weeksBack.toLong()).plusDays(1)
        val logsByDate = logs.groupBy { it.date }

        val cells = mutableListOf<HeatmapCell>()
        var current = from
        while (!current.isAfter(today)) {
            val dayLogs = logsByDate[current] ?: emptyList()
            val intensity = when {
                dayLogs.isEmpty() -> 0
                dayLogs.any { it.status == LogStatus.DONE } -> 4
                dayLogs.any { it.status == LogStatus.SKIPPED } -> 1
                else -> 0
            }
            cells.add(HeatmapCell(current, intensity))
            current = current.plusDays(1)
        }
        return cells
    }
}
