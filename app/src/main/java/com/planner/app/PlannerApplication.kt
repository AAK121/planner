package com.planner.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import com.planner.app.notifications.NotificationChannels
import com.planner.app.notifications.workers.DailyPlanWorker
import com.planner.app.notifications.workers.ScheduledReminderWorker
import com.planner.app.notifications.workers.StreakCheckWorker
import com.planner.app.notifications.workers.WeeklyInsightWorker
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class PlannerApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        NotificationChannels.createAll(this)
        // Worker scheduling is not needed for the first frame; push it off the Main thread.
        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            val wm = WorkManager.getInstance(this@PlannerApplication)
            ScheduledReminderWorker.schedule(wm)
            StreakCheckWorker.schedule(wm)
            WeeklyInsightWorker.schedule(wm)
            DailyPlanWorker.schedule(wm)
        }
    }
}
