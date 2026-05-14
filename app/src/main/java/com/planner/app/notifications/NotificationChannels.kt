package com.planner.app.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationChannels {
    const val REMINDERS    = "planner_reminders"
    const val STREAK_RISK  = "planner_streak_risk"
    const val STREAK_BROKE = "planner_streak_broke"
    const val MISSED_DAY   = "planner_missed_day"
    const val CELEBRATION  = "planner_celebration"
    const val WEEKLY_INSIGHT = "planner_weekly_insight"
    const val SUGGESTION   = "planner_suggestion"

    fun createAll(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)

        listOf(
            Triple(REMINDERS, "Activity Reminders", NotificationManager.IMPORTANCE_DEFAULT),
            Triple(STREAK_RISK, "Streak at Risk", NotificationManager.IMPORTANCE_HIGH),
            Triple(STREAK_BROKE, "Streak Broken", NotificationManager.IMPORTANCE_DEFAULT),
            Triple(MISSED_DAY, "Missed Day", NotificationManager.IMPORTANCE_LOW),
            Triple(CELEBRATION, "Celebrations", NotificationManager.IMPORTANCE_HIGH),
            Triple(WEEKLY_INSIGHT, "Weekly Insights", NotificationManager.IMPORTANCE_DEFAULT),
            Triple(SUGGESTION, "Suggestions", NotificationManager.IMPORTANCE_LOW),
        ).forEach { (id, name, importance) ->
            manager.createNotificationChannel(NotificationChannel(id, name, importance))
        }
    }
}
