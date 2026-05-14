package com.planner.app.notifications.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.planner.app.data.local.datastore.PreferencesDataStore
import com.planner.app.domain.repository.ActivityRepository
import com.planner.app.domain.repository.LlmRepository
import com.planner.app.domain.repository.LogRepository
import com.planner.app.notifications.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.util.concurrent.TimeUnit

@HiltWorker
class WeeklyInsightWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val activityRepository: ActivityRepository,
    private val logRepository: LogRepository,
    private val llmRepository: LlmRepository,
    private val prefs: PreferencesDataStore,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        if (!prefs.llmCloudEnabled.first()) return Result.success()

        val today = LocalDate.now()
        val from = today.minusDays(7)
        val logs = logRepository.getAllInRange(from, today)
        val activities = activityRepository.getAll()

        val summary = activities.joinToString("\n") { activity ->
            val actLogs = logs.filter { it.activityId == activity.id }
            "${activity.name}: ${actLogs.size} logs this week"
        }

        val prompt = """
            Based on this week's activity summary, provide a brief, encouraging insight (2-3 sentences):
            $summary
            Focus on progress, patterns, and one actionable suggestion.
        """.trimIndent()

        val result = llmRepository.generateCloud(prompt)
        result.getOrNull()?.let { insight ->
            NotificationHelper.showWeeklyInsight(context, insight, 9999)
        }

        return if (result.isSuccess) Result.success() else Result.retry()
    }

    companion object {
        private const val WORK_NAME = "planner_weekly_insight"

        fun schedule(workManager: WorkManager) {
            val request = PeriodicWorkRequestBuilder<WeeklyInsightWorker>(7, TimeUnit.DAYS)
                .build()
            workManager.enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
        }
    }
}
