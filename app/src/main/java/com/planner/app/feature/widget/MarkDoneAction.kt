package com.planner.app.feature.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import com.planner.app.core.utils.newId
import com.planner.app.domain.model.ActivityLog
import com.planner.app.domain.model.LogStatus
import dagger.hilt.android.EntryPointAccessors
import java.time.Instant
import java.time.LocalDate

/**
 * Glance action callback that toggles an activity's DONE / PENDING status for
 * today, using the same look-up-or-create pattern as `HomeViewModel.toggleDone`.
 * After writing, re-renders the widget so the row's check flips.
 */
class MarkDoneAction : ActionCallback {

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val activityId = parameters[ACTIVITY_ID] ?: return
        val entry = EntryPointAccessors.fromApplication(
            context.applicationContext,
            WidgetEntryPoint::class.java,
        )
        val today = LocalDate.now()
        val existing = entry.logRepository().getForActivityAndDay(activityId, today)
        // Toggle based on the latest known state — including any prior optimistic
        // value, so rapid taps work correctly.
        val effectiveStatus = entry.optimisticToggles().get(activityId) ?: existing?.status
        val newStatus = if (effectiveStatus == LogStatus.DONE) LogStatus.PENDING else LogStatus.DONE

        // 1) Push optimistic state immediately — widget recomposes within ms.
        entry.optimisticToggles().set(activityId, newStatus)

        // 2) Persist to DB. Room flow will emit shortly; the widget composable
        //    auto-clears the optimistic entry once the DB status catches up.
        val log = existing?.copy(status = newStatus) ?: ActivityLog(
            id = newId(),
            activityId = activityId,
            date = today,
            status = newStatus,
            durationMinutes = 0,
            note = "",
            createdAt = Instant.now().toEpochMilli(),
        )
        entry.logActivity().invoke(log)
    }

    companion object {
        val ACTIVITY_ID = ActionParameters.Key<String>("activity_id")
    }
}
