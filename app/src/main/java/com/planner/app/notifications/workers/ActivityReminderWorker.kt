package com.planner.app.notifications.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.planner.app.data.local.datastore.PreferencesDataStore
import com.planner.app.domain.repository.ActivityRepository
import com.planner.app.notifications.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class ActivityReminderWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val activityRepository: ActivityRepository,
    private val prefs: PreferencesDataStore,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        if (!prefs.notifReminders.first()) return Result.success()

        val activityId = inputData.getString(KEY_ACTIVITY_ID) ?: return Result.success()
        val minutesUntil = inputData.getInt(KEY_MINUTES_UNTIL, 10)
        val activity = activityRepository.getById(activityId) ?: return Result.success()
        if (activity.isArchived) return Result.success()

        NotificationHelper.showActivityReminder(
            context = context,
            activityId = activity.id,
            activityName = activity.name,
            className = activity.className,
            minutesUntil = minutesUntil,
            notifId = activity.id.hashCode(),
        )
        return Result.success()
    }

    companion object {
        const val KEY_ACTIVITY_ID = "activityId"
        const val KEY_MINUTES_UNTIL = "minutesUntil"
        const val TAG = "activity_reminder"
    }
}
