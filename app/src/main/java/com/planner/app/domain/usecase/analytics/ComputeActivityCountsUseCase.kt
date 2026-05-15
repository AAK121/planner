package com.planner.app.domain.usecase.analytics

import com.planner.app.domain.model.ActivityLog
import com.planner.app.domain.model.LogStatus
import com.planner.app.domain.model.PeriodCount
import java.time.LocalDate
import javax.inject.Inject

class ComputeActivityCountsUseCase @Inject constructor() {
    operator fun invoke(
        logs: List<ActivityLog>,
        today: LocalDate = LocalDate.now(),
    ): List<PeriodCount> {
        fun count(days: Long, label: String): PeriodCount {
            val from = today.minusDays(days - 1)
            var done = 0
            var skip = 0
            for (log in logs) {
                if (log.date < from || log.date > today) continue
                when (log.status) {
                    LogStatus.DONE -> done++
                    LogStatus.SKIPPED -> skip++
                    else -> { /* PENDING is excluded */ }
                }
            }
            return PeriodCount(label = label, done = done, skip = skip)
        }
        return listOf(
            count(7, "Week"),
            count(30, "Month"),
            count(90, "3 months"),
            count(270, "9 months"),
            count(365, "12 months"),
        )
    }
}
