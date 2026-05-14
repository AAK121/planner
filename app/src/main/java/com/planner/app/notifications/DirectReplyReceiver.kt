package com.planner.app.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.RemoteInput
import com.planner.app.domain.model.ActivityLog
import com.planner.app.domain.model.LogStatus
import com.planner.app.domain.usecase.log.LogActivityUseCase
import com.planner.app.core.utils.newId
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject

@AndroidEntryPoint
class DirectReplyReceiver : BroadcastReceiver() {

    @Inject
    lateinit var logActivity: LogActivityUseCase

    override fun onReceive(context: Context, intent: Intent) {
        val activityId = intent.getStringExtra(NotificationHelper.EXTRA_ACTIVITY_ID) ?: return
        val note = RemoteInput.getResultsFromIntent(intent)
            ?.getCharSequence(NotificationHelper.KEY_DIRECT_REPLY)?.toString() ?: ""

        CoroutineScope(Dispatchers.IO).launch {
            logActivity(
                ActivityLog(
                    id = newId(),
                    activityId = activityId,
                    date = LocalDate.now(),
                    status = LogStatus.DONE,
                    durationMinutes = 0,
                    note = note,
                    createdAt = Instant.now().toEpochMilli(),
                )
            )
        }
    }
}
