package com.planner.app.domain.usecase.analytics

import com.planner.app.domain.model.ActivityLog
import com.planner.app.domain.model.LogStatus
import java.time.LocalDate
import javax.inject.Inject

class ComputeStreakUseCase @Inject constructor() {
    data class StreakResult(val current: Int, val longest: Int)

    operator fun invoke(logs: List<ActivityLog>, today: LocalDate = LocalDate.now()): StreakResult {
        val doneDays = logs
            .filter { it.status == LogStatus.DONE }
            .map { it.date }
            .toSortedSet()

        var current = 0
        var check = today
        while (doneDays.contains(check)) {
            current++
            check = check.minusDays(1)
        }

        var longest = 0
        var run = 0
        var prev: LocalDate? = null
        for (day in doneDays) {
            if (prev != null && day == prev!!.plusDays(1)) {
                run++
            } else {
                run = 1
            }
            if (run > longest) longest = run
            prev = day
        }

        return StreakResult(current, longest)
    }
}
