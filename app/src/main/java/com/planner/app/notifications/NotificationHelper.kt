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
    const val EXTRA_ACTIVITY_NAME = "activity_name"
    const val EXTRA_CLASS_NAME = "class_name"
    const val EXTRA_NOTIF_ID = "notif_id"
    const val ACTION_DIRECT_REPLY = "com.planner.app.DIRECT_REPLY"
    const val ACTION_QUICK_DONE = "com.planner.app.QUICK_DONE"
    const val ACTION_QUICK_SKIP = "com.planner.app.QUICK_SKIP"

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

    /**
     * Per-activity reminder fired ~10 minutes before a scheduled time.
     * Title placeholders:
     *   With class:    "{className} · {activityName} in {minutesUntil} min"
     *   Without class: "{activityName} in {minutesUntil} min"
     */
    fun showActivityReminder(
        context: Context,
        activityId: String,
        activityName: String,
        className: String?,
        minutesUntil: Int,
        notifId: Int,
    ) {
        val title = when {
            minutesUntil <= 0 && className.isNullOrBlank() -> "$activityName — now"
            minutesUntil <= 0 -> "$className · $activityName — now"
            className.isNullOrBlank() -> "$activityName in $minutesUntil min"
            else -> "$className · $activityName in $minutesUntil min"
        }
        val body = when {
            minutesUntil <= 0 -> "Time to do this. Tap to log when you're done."
            className.isNullOrBlank() -> "Time for $activityName. Tap to log when you're done."
            else -> "Get ready. Tap to log when you're done."
        }

        val openIntent = PendingIntent.getActivity(
            context, notifId,
            Intent(context, MainActivity::class.java).apply { putExtra(EXTRA_ACTIVITY_ID, activityId) },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        // Explicit intent: target DirectReplyReceiver by class, set action so the
        // receiver can dispatch. Implicit intents fail to deliver on Android 8+
        // without an <intent-filter> declared in the manifest.
        val doneIntent = PendingIntent.getBroadcast(
            context, notifId + 10000,
            Intent(context, DirectReplyReceiver::class.java).apply {
                action = ACTION_QUICK_DONE
                putExtra(EXTRA_ACTIVITY_ID, activityId)
                putExtra(EXTRA_NOTIF_ID, notifId)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val skipIntent = PendingIntent.getBroadcast(
            context, notifId + 20000,
            Intent(context, DirectReplyReceiver::class.java).apply {
                action = ACTION_QUICK_SKIP
                putExtra(EXTRA_ACTIVITY_ID, activityId)
                putExtra(EXTRA_NOTIF_ID, notifId)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val postponeIntent = PendingIntent.getActivity(
            context, notifId + 30000,
            Intent(context, PostponeActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                putExtra(EXTRA_ACTIVITY_ID, activityId)
                putExtra(EXTRA_ACTIVITY_NAME, activityName)
                putExtra(EXTRA_CLASS_NAME, className)
                putExtra(EXTRA_NOTIF_ID, notifId)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, NotificationChannels.REMINDERS)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(body)
            .setContentIntent(openIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .addAction(android.R.drawable.ic_menu_save, "Done", doneIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Skip", skipIntent)
            .addAction(android.R.drawable.ic_menu_recent_history, "Postpone", postponeIntent)
            .setAutoCancel(true)
            .build()

        context.getSystemService(NotificationManager::class.java).notify(notifId, notification)
    }

    /**
     * Daily plan summary at ~8 AM. Body placeholders:
     *   0 activities:      no notification (caller should skip)
     *   1 (no class):      "{activityName}."
     *   1 (with class):    "{className} · {activityName}."
     *   2-3:               "{name1}, {name2}, {name3}." (uses "{className} · name" form per activity)
     *   4+:                "{name1}, {name2}, {name3} and {count - 3} more."
     */
    fun showDailyPlan(
        context: Context,
        items: List<DailyPlanItem>,
        notifId: Int,
    ) {
        if (items.isEmpty()) return

        fun display(item: DailyPlanItem) =
            if (item.className.isNullOrBlank()) item.name
            else "${item.className} · ${item.name}"

        val count = items.size
        val title = if (count == 1) "Today's plan" else "Today's plan ($count activities)"
        val body = when {
            count == 1 -> "${display(items[0])}."
            count <= 3 -> items.joinToString(", ") { display(it) } + "."
            else -> {
                val first3 = items.take(3).joinToString(", ") { display(it) }
                "$first3 and ${count - 3} more."
            }
        }

        val openIntent = PendingIntent.getActivity(
            context, notifId,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, NotificationChannels.REMINDERS)
            .setSmallIcon(android.R.drawable.ic_menu_my_calendar)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(openIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .build()

        context.getSystemService(NotificationManager::class.java).notify(notifId, notification)
    }

    data class DailyPlanItem(val name: String, val className: String?)
}
