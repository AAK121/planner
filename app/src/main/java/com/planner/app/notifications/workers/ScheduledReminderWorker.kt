package com.planner.app.notifications.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.planner.app.notifications.ReminderScheduler
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.TimeUnit

/**
 * Daily refresh — runs once just past midnight to (re-)register every activity's
 * alarms for the new day. Immediate scheduling for "user just changed something"
 * paths goes through `ReminderScheduler` directly (no worker latency).
 */
@HiltWorker
class ScheduledReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val scheduler: ReminderScheduler,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        scheduler.scheduleAllForToday()
        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "planner_reminders"

        fun schedule(workManager: WorkManager) {
            val nowMillis = System.currentTimeMillis()
            val nextMidnight = LocalDate.now().plusDays(1).atStartOfDay()
                .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val delayToMidnight = (nextMidnight - nowMillis).coerceAtLeast(0L)

            val periodic = PeriodicWorkRequestBuilder<ScheduledReminderWorker>(24, TimeUnit.HOURS)
                .setInitialDelay(delayToMidnight, TimeUnit.MILLISECONDS)
                .build()
            workManager.enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.UPDATE, periodic)
        }
    }
}
