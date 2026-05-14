package com.planner.app.notifications

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput
import com.planner.app.MainActivity
import com.planner.app.R

object NotificationHelper {
    const val KEY_DIRECT_REPLY = "key_direct_reply"
    const val EXTRA_ACTIVITY_ID = "activity_id"
    const val ACTION_DIRECT_REPLY = "com.planner.app.DIRECT_REPLY"
    const val ACTION_QUICK_DONE = "com.planner.app.QUICK_DONE"

    fun showReminder(context: Context, activityId: String, activityName: String, notifId: Int) {
        val openIntent = PendingIntent.getActivity(
            context, notifId,
            Intent(context, MainActivity::class.java).apply { putExtra(EXTRA_ACTIVITY_ID, activityId) },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val doneIntent = PendingIntent.getBroadcast(
            context, notifId + 10000,
            Intent(ACTION_QUICK_DONE).apply { putExtra(EXTRA_ACTIVITY_ID, activityId) },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val remoteInput = RemoteInput.Builder(KEY_DIRECT_REPLY).setLabel("Note (optional)").build()
        val replyAction = NotificationCompat.Action.Builder(
            android.R.drawable.ic_menu_send, "Log done",
            PendingIntent.getBroadcast(
                context, notifId + 20000,
                Intent(ACTION_DIRECT_REPLY).apply { putExtra(EXTRA_ACTIVITY_ID, activityId) },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE,
            )
        ).addRemoteInput(remoteInput).build()

        val notification = NotificationCompat.Builder(context, NotificationChannels.REMINDERS)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(activityName)
            .setContentText("Time to log your activity.")
            .setContentIntent(openIntent)
            .addAction(doneIntent.let {
                NotificationCompat.Action(android.R.drawable.ic_menu_save, "Done", it)
            })
            .addAction(replyAction)
            .setAutoCancel(true)
            .build()

        context.getSystemService(NotificationManager::class.java)
            .notify(notifId, notification)
    }

    fun showStreakAtRisk(context: Context, activityName: String, streak: Int, notifId: Int) {
        val notification = NotificationCompat.Builder(context, NotificationChannels.STREAK_RISK)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Streak at risk 🔥")
            .setContentText("Log $activityName today to keep your $streak-day streak alive.")
            .setAutoCancel(true)
            .build()
        context.getSystemService(NotificationManager::class.java).notify(notifId, notification)
    }

    fun showCelebration(context: Context, activityName: String, days: Int, notifId: Int) {
        val notification = NotificationCompat.Builder(context, NotificationChannels.CELEBRATION)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("$days days! 🎉")
            .setContentText("You've completed $activityName $days days in a row. Keep going!")
            .setAutoCancel(true)
            .build()
        context.getSystemService(NotificationManager::class.java).notify(notifId, notification)
    }

    fun showWeeklyInsight(context: Context, insight: String, notifId: Int) {
        val notification = NotificationCompat.Builder(context, NotificationChannels.WEEKLY_INSIGHT)
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setContentTitle("Your weekly insight")
            .setStyle(NotificationCompat.BigTextStyle().bigText(insight))
            .setAutoCancel(true)
            .build()
        context.getSystemService(NotificationManager::class.java).notify(notifId, notification)
    }
}
