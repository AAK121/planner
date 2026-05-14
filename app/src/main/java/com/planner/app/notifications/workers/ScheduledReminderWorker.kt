package com.planner.app.notifications.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.planner.app.domain.repository.ActivityRepository
import com.planner.app.notifications.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalDate
import java.util.concurrent.TimeUnit

@HiltWorker
class ScheduledReminderWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val activityRepository: ActivityRepository,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val today = LocalDate.now()
        activityRepository.getAll().forEachIndexed { index, activity ->
            if (activity.schedule.daysOfWeek.contains(today.dayOfWeek.value) &&
                activity.schedule.timesOfDay.isNotEmpty()
            ) {
                NotificationHelper.showReminder(
                    context = context,
                    activityId = activity.id,
                    activityName = activity.name,
                    notifId = index + 1000,
                )
            }
        }
        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "planner_reminders"

        fun schedule(workManager: WorkManager) {
            val request = PeriodicWorkRequestBuilder<ScheduledReminderWorker>(1, TimeUnit.HOURS)
                .setConstraints(Constraints.Builder().setRequiresBatteryNotLow(false).build())
                .build()
            workManager.enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
        }
    }
}
