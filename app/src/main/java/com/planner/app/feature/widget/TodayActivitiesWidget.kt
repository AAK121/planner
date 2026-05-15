package com.planner.app.feature.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.planner.app.MainActivity
import com.planner.app.domain.model.Activity
import com.planner.app.domain.model.ActivityLog
import com.planner.app.domain.model.LogStatus
import dagger.hilt.android.EntryPointAccessors
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class TodayActivitiesWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entry = EntryPointAccessors.fromApplication(
            context.applicationContext, WidgetEntryPoint::class.java,
        )
        provideContent {
            // collectAsState keeps the widget reactive: every Room flow emission
            // (mark-done from app, notification action, widget tap, anywhere else)
            // triggers recomposition and a fresh RemoteViews push to the launcher.
            // No manual updateAll() hooks needed.
            val today = LocalDate.now()
            val activities by entry.getTodayActivities().invoke(today)
                .collectAsState(initial = emptyList())
            val logs by entry.logRepository().observeForDay(today)
                .collectAsState(initial = emptyList())
            val optimistic by entry.optimisticToggles().state
                .collectAsState(initial = emptyMap())
            val logsById = remember(logs) { logs.associateBy { it.activityId } }

            // Once the DB log catches up to an optimistic value, retire the
            // optimistic entry. Keeps the map from drifting from reality and
            // ensures future taps read a coherent baseline.
            LaunchedEffect(logs) {
                val confirmed = optimistic.filter { (id, status) ->
                    logsById[id]?.status == status
                }.keys
                if (confirmed.isNotEmpty()) {
                    entry.optimisticToggles().clear(confirmed)
                }
            }

            WidgetBody(today, activities, logsById, optimistic)
        }
    }
}

@Composable
private fun WidgetBody(
    today: LocalDate,
    activities: List<Activity>,
    logsById: Map<String, ActivityLog>,
    optimistic: Map<String, LogStatus>,
) {
    GlanceTheme {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.background)
                .cornerRadius(16.dp)
                .padding(12.dp),
        ) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = today.format(headerFormatter),
                    style = TextStyle(
                        color = GlanceTheme.colors.onBackground,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    modifier = GlanceModifier.defaultWeight(),
                )
                Text(
                    text = "Open",
                    style = TextStyle(
                        color = GlanceTheme.colors.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                    modifier = GlanceModifier
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .clickable(actionStartActivity<MainActivity>()),
                )
            }
            Spacer(GlanceModifier.height(8.dp))

            if (activities.isEmpty()) {
                Text(
                    text = "Nothing scheduled today",
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurfaceVariant,
                        fontSize = 14.sp,
                    ),
                )
            } else {
                // Stable partition: pending first, done last. Kotlin's sortedBy is
                // stable so original order within each group is preserved — checking
                // an item slides it to the bottom; unchecking it returns to its
                // original slot in the pending group.
                val ordered = activities.sortedBy { a ->
                    val status = optimistic[a.id] ?: logsById[a.id]?.status
                    if (status == LogStatus.DONE) 1 else 0
                }
                LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
                    items(
                        items = ordered,
                        itemId = { it.id.hashCode().toLong() },
                    ) { activity ->
                        // Optimistic override wins over the DB-derived status so the
                        // row flips immediately on tap, before Room emits the new value.
                        val effectiveStatus = optimistic[activity.id]
                            ?: logsById[activity.id]?.status
                        ActivityRow(activity = activity, effectiveStatus = effectiveStatus)
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivityRow(activity: Activity, effectiveStatus: LogStatus?) {
    val isDone = effectiveStatus == LogStatus.DONE
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            // `clickable` must wrap the whole visible row. In Glance, placing it
            // AFTER `padding`/`background` shrinks the click target to the inner
            // content area — taps on the card edges then do nothing.
            .clickable(
                actionRunCallback<MarkDoneAction>(
                    actionParametersOf(MarkDoneAction.ACTIVITY_ID to activity.id),
                )
            )
            .cornerRadius(12.dp)
            .background(
                if (isDone) GlanceTheme.colors.surfaceVariant
                else GlanceTheme.colors.primaryContainer
            )
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = if (isDone) "☑" else "☐",
            style = TextStyle(
                color = GlanceTheme.colors.onPrimaryContainer,
                fontSize = 18.sp,
            ),
            modifier = GlanceModifier.padding(end = 10.dp),
        )
        Column(modifier = GlanceModifier.defaultWeight()) {
            Text(
                text = activity.name,
                style = TextStyle(
                    color = if (isDone) GlanceTheme.colors.onSurfaceVariant
                            else GlanceTheme.colors.onPrimaryContainer,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                ),
                maxLines = 1,
            )
            val subtitle = buildSubtitle(activity)
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurfaceVariant,
                        fontSize = 11.sp,
                    ),
                    maxLines = 1,
                )
            }
        }
    }
}

private fun buildSubtitle(activity: Activity): String {
    val parts = mutableListOf<String>()
    activity.className?.takeIf { it.isNotBlank() }?.let { parts.add(it) }
    activity.schedule.timesOfDay.firstOrNull()?.let { parts.add(formatTime(it)) }
    return parts.joinToString(" · ")
}

private fun formatTime(hhmm: String): String {
    val parts = hhmm.split(":")
    val h = parts.getOrNull(0)?.toIntOrNull() ?: return hhmm
    val m = parts.getOrNull(1)?.toIntOrNull() ?: return hhmm
    val amPm = if (h < 12) "AM" else "PM"
    val displayH = when {
        h == 0 -> 12
        h > 12 -> h - 12
        else -> h
    }
    return "%d:%02d %s".format(displayH, m, amPm)
}

private val headerFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE, MMM d")
