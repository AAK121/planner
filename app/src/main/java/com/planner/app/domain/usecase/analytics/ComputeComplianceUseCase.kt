package com.planner.app.domain.usecase.analytics

import com.planner.app.domain.model.Activity
import com.planner.app.domain.model.ActivityLog
import com.planner.app.domain.model.ActivityType
import com.planner.app.domain.model.LogStatus
import java.time.LocalDate
import javax.inject.Inject

class ComputeComplianceUseCase @Inject constructor() {
    data class ComplianceResult(
        val rate7d: Float,
        val rate30d: Float,
        val rate90d: Float,
        val rateAll: Float,
        val totalDone: Int,
        val totalLogged: Int,
    )

    operator fun invoke(
        activity: Activity,
        logs: List<ActivityLog>,
        today: LocalDate = LocalDate.now(),
    ): ComplianceResult {
        val doneStatuses = setOf(LogStatus.DONE)

        fun rateForDays(days: Long): Float {
            val from = today.minusDays(days - 1)
            val scheduledDays = (0 until days).count { offset ->
                val date = from.plusDays(offset)
                activity.type == ActivityType.RECURRING &&
                    activity.schedule.daysOfWeek.contains(date.dayOfWeek.value)
            }
            if (scheduledDays == 0) return 0f
            val done = logs.count { log ->
                log.date >= from && log.date <= today && log.status in doneStatuses
            }
            return done.toFloat() / scheduledDays.toFloat()
        }

        val totalDone = logs.count { it.status in doneStatuses }
        val allLogged = logs.size

        return ComplianceResult(
            rate7d = rateForDays(7),
            rate30d = rateForDays(30),
            rate90d = rateForDays(90),
            rateAll = if (allLogged == 0) 0f else totalDone.toFloat() / allLogged,
            totalDone = totalDone,
            totalLogged = allLogged,
        )
    }
}
