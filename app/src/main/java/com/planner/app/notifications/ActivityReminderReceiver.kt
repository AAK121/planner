package com.planner.app.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Fired by AlarmManager at the precise scheduled time. All data needed to render the
 * notification is in the intent extras — no DB access required, which keeps onReceive
 * fast and lets the receiver run without Hilt injection.
 */
class ActivityReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val activityId = intent.getStringExtra(EXTRA_ACTIVITY_ID) ?: return
        val activityName = intent.getStringExtra(EXTRA_ACTIVITY_NAME) ?: return
        val className = intent.getStringExtra(EXTRA_CLASS_NAME)
        val minutesUntil = intent.getIntExtra(EXTRA_MINUTES_UNTIL, 10)

        // Distinct notifId per (activity, minutesUntil) so the "in 10 min" warning and
        // the at-time notification don't overwrite each other in the tray.
        NotificationHelper.showActivityReminder(
            context = context,
            activityId = activityId,
            activityName = activityName,
            className = className,
            minutesUntil = minutesUntil,
            notifId = "$activityId:$minutesUntil".hashCode(),
        )
    }

    companion object {
        const val EXTRA_ACTIVITY_ID = "activity_id"
        const val EXTRA_ACTIVITY_NAME = "activity_name"
        const val EXTRA_CLASS_NAME = "class_name"
        const val EXTRA_MINUTES_UNTIL = "minutes_until"
    }
}
