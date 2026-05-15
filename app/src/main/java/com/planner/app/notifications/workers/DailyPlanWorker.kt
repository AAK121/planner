package com.planner.app.notifications.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.planner.app.data.local.datastore.PreferencesDataStore
import com.planner.app.domain.model.ActivityType
import com.planner.app.domain.repository.ActivityRepository
import com.planner.app.notifications.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

/**
 * Fires one daily plan notification listing today's scheduled activities.
 * Runs periodically with a 24h interval, anchored to 8 AM local time.
 */
@HiltWorker
class DailyPlanWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val activityRepository: ActivityRepository,
    private val prefs: PreferencesDataStore,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        if (!prefs.notifDailyPlan.first()) return Result.success()
        val today = LocalDate.now()
        val dow = today.dayOfWeek.value

        val activitiesToday = activityRepository.getAll().filter { activity ->
            if (activity.isArchived) return@filter false
            when (activity.type) {
                ActivityType.RECURRING -> activity.schedule.daysOfWeek.contains(dow)
                ActivityType.OCCASIONAL -> activity.schedule.daysOfWeek.contains(dow)
                ActivityType.ONE_OFF -> {
                    val due = activity.schedule.dueDate ?: return@filter false
                    val dueDate = java.time.Instant.ofEpochMilli(due)
                        .atZone(ZoneId.systemDefault()).toLocalDate()
                    dueDate == today
                }
            }
        }

        if (activitiesToday.isEmpty()) return Result.success()

        val items = activitiesToday.map {
            NotificationHelper.DailyPlanItem(name = it.name, className = it.className)
        }
        NotificationHelper.showDailyPlan(context, items, NOTIF_ID)
        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "planner_daily_plan"
        private const val NOTIF_ID = 8200
        private const val FIRE_HOUR = 8  // 8 AM local

        fun schedule(workManager: WorkManager) {
            val now = LocalDateTime.now()
            var next = LocalDateTime.of(now.toLocalDate(), LocalTime.of(FIRE_HOUR, 0))
            if (!next.isAfter(now)) next = next.plusDays(1)
            val delayMillis = next.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() -
                System.currentTimeMillis()

            val request = PeriodicWorkRequestBuilder<DailyPlanWorker>(24, TimeUnit.HOURS)
                .setInitialDelay(delayMillis.coerceAtLeast(0L), TimeUnit.MILLISECONDS)
                .build()
            workManager.enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.UPDATE, request)
        }
    }
}
