package com.planner.app.notifications.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.planner.app.domain.repository.ActivityRepository
import com.planner.app.domain.repository.LogRepository
import com.planner.app.domain.usecase.analytics.ComputeStreakUseCase
import com.planner.app.notifications.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalDate
import java.util.concurrent.TimeUnit

@HiltWorker
class StreakCheckWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val activityRepository: ActivityRepository,
    private val logRepository: LogRepository,
    private val computeStreak: ComputeStreakUseCase,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val today = LocalDate.now()
        activityRepository.getAll().forEachIndexed { index, activity ->
            val logs = logRepository.getForActivity(activity.id)
            val streak = computeStreak(logs, today)
            val hasLoggedToday = logRepository.getForActivityAndDay(activity.id, today) != null
            if (streak.current >= 3 && !hasLoggedToday) {
                NotificationHelper.showStreakAtRisk(context, activity.name, streak.current, index + 2000)
            }
            if (streak.current in listOf(7, 30, 100)) {
                NotificationHelper.showCelebration(context, activity.name, streak.current, index + 3000)
            }
        }
        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "planner_streak_check"

        fun schedule(workManager: WorkManager) {
            val request = PeriodicWorkRequestBuilder<StreakCheckWorker>(24, TimeUnit.HOURS)
                .build()
            workManager.enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
        }
    }
}
