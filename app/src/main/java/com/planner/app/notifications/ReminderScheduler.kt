package com.planner.app.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.planner.app.data.local.datastore.PreferencesDataStore
import com.planner.app.domain.model.Activity
import com.planner.app.domain.repository.ActivityRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Owns the AlarmManager scheduling logic for activity reminders.
 *
 * Two alarms per (activity, time): one `reminderMinutesBefore` minutes before, one
 * AT the time. Alarms fire `ActivityReminderReceiver`, which renders the
 * notification without any DB access.
 *
 * Callable directly from ViewModels (no WorkManager latency) and from the daily
 * `ScheduledReminderWorker` for the midnight refresh.
 */
@Singleton
class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val activityRepository: ActivityRepository,
    private val prefs: PreferencesDataStore,
) {

    suspend fun scheduleAllForToday() {
        val am = context.getSystemService(AlarmManager::class.java) ?: return
        val activities = activityRepository.getAll()

        if (!prefs.notifReminders.first()) {
            cancelAlarms(am, activities)
            return
        }

        val today = LocalDate.now()
        val now = LocalDateTime.now()

        for (activity in activities) {
            if (activity.isArchived) continue
            val schedule = activity.schedule
            if (schedule.timesOfDay.isEmpty()) continue
            if (!schedule.daysOfWeek.contains(today.dayOfWeek.value)) continue

            for (timeStr in schedule.timesOfDay) {
                val time = parseTime(timeStr) ?: continue
                val atTime = LocalDateTime.of(today, time)
                val beforeTime = atTime.minusMinutes(schedule.reminderMinutesBefore.toLong())

                scheduleOne(
                    am, activity, timeStr, beforeTime, now,
                    minutesUntil = schedule.reminderMinutesBefore, suffix = "before",
                )
                scheduleOne(
                    am, activity, timeStr, atTime, now,
                    minutesUntil = 0, suffix = "at",
                )
            }
        }
    }

    suspend fun cancelAll() {
        val am = context.getSystemService(AlarmManager::class.java) ?: return
        cancelAlarms(am, activityRepository.getAll())
    }

    /**
     * One-shot alarm for a user-chosen postpone time today. Temporary by design —
     * does NOT modify the activity's stored schedule, so tomorrow's reminders fire
     * at the original times.
     *
     * Implementation notes:
     * - Stable request code per activity (no time in the hash). Re-postponing the
     *   same activity REPLACES the previous postpone alarm via FLAG_UPDATE_CURRENT.
     * - Cancels today's original `before`/`at` alarms for the activity so they
     *   don't fire alongside the postponed one.
     * - Uses `setAlarmClock`, the most precise alarm method — exempt from Doze /
     *   battery-saver throttling. (`setExactAndAllowWhileIdle` is rate-limited to
     *   once per ~15 min under Doze; that's why the postponed notification was
     *   firing minutes late before.)
     */
    suspend fun schedulePostponed(
        activityId: String,
        activityName: String,
        className: String?,
        time: LocalTime,
    ) {
        val am = context.getSystemService(AlarmManager::class.java) ?: return
        val fireAt = LocalDateTime.of(LocalDate.now(), time)
        if (!fireAt.isAfter(LocalDateTime.now())) return
        val triggerAtMillis = fireAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        // Cancel today's original before/at alarms for this activity — postpone
        // replaces them. The daily midnight refresh will re-add them tomorrow.
        activityRepository.getById(activityId)?.schedule?.timesOfDay?.forEach { timeStr ->
            for (suffix in listOf("before", "at")) {
                val cancelIntent = Intent(context, ActivityReminderReceiver::class.java)
                val rc = ("$activityId|$timeStr|$suffix").hashCode()
                val pi = PendingIntent.getBroadcast(
                    context, rc, cancelIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
                am.cancel(pi)
            }
        }

        val intent = Intent(context, ActivityReminderReceiver::class.java).apply {
            putExtra(ActivityReminderReceiver.EXTRA_ACTIVITY_ID, activityId)
            putExtra(ActivityReminderReceiver.EXTRA_ACTIVITY_NAME, activityName)
            putExtra(ActivityReminderReceiver.EXTRA_CLASS_NAME, className)
            putExtra(ActivityReminderReceiver.EXTRA_MINUTES_UNTIL, 0)
        }
        // Stable RC: one postpone slot per activity. Re-postpone overwrites.
        val rc = ("$activityId|postpone").hashCode()
        val pi = PendingIntent.getBroadcast(
            context, rc, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val showIntent = PendingIntent.getActivity(
            context, rc + 99_999,
            Intent(context, com.planner.app.MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        am.setAlarmClock(AlarmManager.AlarmClockInfo(triggerAtMillis, showIntent), pi)
    }

    /** Explicitly clears a previously postponed alarm for an activity. */
    fun cancelPostponed(activityId: String) {
        val am = context.getSystemService(AlarmManager::class.java) ?: return
        val intent = Intent(context, ActivityReminderReceiver::class.java)
        val rc = ("$activityId|postpone").hashCode()
        val pi = PendingIntent.getBroadcast(
            context, rc, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        am.cancel(pi)
    }

    /**
     * Cancel every pending alarm tied to this activity — every (timeStr, before/at)
     * pair plus the postpone slot. Call this before deleting an activity from the
     * DB so its reminders don't fire afterwards.
     */
    fun cancelForActivity(activity: Activity) {
        val am = context.getSystemService(AlarmManager::class.java) ?: return
        cancelAlarms(am, listOf(activity))
        cancelPostponed(activity.id)
    }

    private fun scheduleOne(
        am: AlarmManager,
        activity: Activity,
        timeStr: String,
        fireAt: LocalDateTime,
        now: LocalDateTime,
        minutesUntil: Int,
        suffix: String,
    ) {
        if (!fireAt.isAfter(now)) return
        val triggerAtMillis = fireAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val intent = Intent(context, ActivityReminderReceiver::class.java).apply {
            putExtra(ActivityReminderReceiver.EXTRA_ACTIVITY_ID, activity.id)
            putExtra(ActivityReminderReceiver.EXTRA_ACTIVITY_NAME, activity.name)
            putExtra(ActivityReminderReceiver.EXTRA_CLASS_NAME, activity.className)
            putExtra(ActivityReminderReceiver.EXTRA_MINUTES_UNTIL, minutesUntil)
        }
        val requestCode = ("${activity.id}|$timeStr|$suffix").hashCode()
        val pi = PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        // setAlarmClock is exempt from Doze + battery-saver throttling that
        // routinely delays setExactAndAllowWhileIdle by tens of seconds.
        val showIntent = PendingIntent.getActivity(
            context, requestCode + 99_999,
            Intent(context, com.planner.app.MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        am.setAlarmClock(AlarmManager.AlarmClockInfo(triggerAtMillis, showIntent), pi)
    }

    private fun cancelAlarms(am: AlarmManager, activities: List<Activity>) {
        for (activity in activities) {
            for (timeStr in activity.schedule.timesOfDay) {
                for (suffix in listOf("before", "at")) {
                    val intent = Intent(context, ActivityReminderReceiver::class.java)
                    val rc = ("${activity.id}|$timeStr|$suffix").hashCode()
                    val pi = PendingIntent.getBroadcast(
                        context, rc, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                    )
                    am.cancel(pi)
                }
            }
        }
    }

    private fun parseTime(value: String): LocalTime? = try {
        LocalTime.parse(value)
    } catch (_: Exception) {
        null
    }
}
