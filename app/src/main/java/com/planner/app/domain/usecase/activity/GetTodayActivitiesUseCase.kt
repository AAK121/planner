package com.planner.app.domain.usecase.activity

import com.planner.app.domain.model.Activity
import com.planner.app.domain.model.ActivityType
import com.planner.app.domain.repository.ActivityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class GetTodayActivitiesUseCase @Inject constructor(
    private val activityRepository: ActivityRepository,
) {
    operator fun invoke(today: LocalDate = LocalDate.now()): Flow<List<Activity>> =
        activityRepository.observeAll().map { all ->
            all.filter { activity -> activity.isScheduledFor(today) }
        }

    private fun Activity.isScheduledFor(date: LocalDate): Boolean = when (type) {
        ActivityType.RECURRING -> schedule.daysOfWeek.contains(date.dayOfWeek.value)
        ActivityType.OCCASIONAL -> true
        ActivityType.ONE_OFF -> {
            val dueDay = schedule.dueDate?.let {
                LocalDate.ofEpochDay(it / 86_400_000)
            }
            dueDay == date
        }
    }
}
