package com.planner.app.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import com.planner.app.core.utils.newId
import com.planner.app.domain.model.ActivityLog
import com.planner.app.domain.model.LogStatus
import com.planner.app.domain.repository.LogRepository
import com.planner.app.domain.usecase.log.LogActivityUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject

@AndroidEntryPoint
class DirectReplyReceiver : BroadcastReceiver() {

    @Inject
    lateinit var logActivity: LogActivityUseCase

    @Inject
    lateinit var logRepository: LogRepository

    override fun onReceive(context: Context, intent: Intent) {
        val activityId = intent.getStringExtra(NotificationHelper.EXTRA_ACTIVITY_ID) ?: return
        val notifId = intent.getIntExtra(NotificationHelper.EXTRA_NOTIF_ID, -1)
        val today = LocalDate.now()

        val targetStatus = when (intent.action) {
            NotificationHelper.ACTION_QUICK_DONE,
            NotificationHelper.ACTION_DIRECT_REPLY -> LogStatus.DONE
            NotificationHelper.ACTION_QUICK_SKIP -> LogStatus.SKIPPED
            else -> return
        }

        // Only ACTION_DIRECT_REPLY carries RemoteInput text; the other paths leave it blank.
        val note = if (intent.action == NotificationHelper.ACTION_DIRECT_REPLY) {
            RemoteInput.getResultsFromIntent(intent)
                ?.getCharSequence(NotificationHelper.KEY_DIRECT_REPLY)?.toString() ?: ""
        } else {
            ""
        }

        // goAsync keeps the receiver alive across the suspending DB call without
        // leaking a global coroutine. Must call pendingResult.finish() when done.
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val existing = logRepository.getForActivityAndDay(activityId, today)
                val log = if (existing != null) {
                    existing.copy(status = targetStatus, note = note.ifEmpty { existing.note })
                } else {
                    ActivityLog(
                        id = newId(),
                        activityId = activityId,
                        date = today,
                        status = targetStatus,
                        durationMinutes = 0,
                        note = note,
                        createdAt = Instant.now().toEpochMilli(),
                    )
                }
                logActivity(log)
                if (notifId != -1) {
                    NotificationManagerCompat.from(context).cancel(notifId)
                }
                // Widget refreshes itself reactively via Room flow + collectAsState.
            } finally {
                pendingResult.finish()
            }
        }
    }
}
