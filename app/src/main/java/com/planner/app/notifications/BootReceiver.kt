package com.planner.app.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.WorkManager
import com.planner.app.notifications.workers.ScheduledReminderWorker

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            ScheduledReminderWorker.schedule(WorkManager.getInstance(context))
        }
    }
}
