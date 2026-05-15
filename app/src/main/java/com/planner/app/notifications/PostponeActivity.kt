package com.planner.app.notifications

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

/**
 * Transparent host for a Material 3 TimePicker dialog. Launched when the user
 * taps the "Postpone" action on a reminder notification. On confirm, schedules
 * a one-shot alarm via [ReminderScheduler.schedulePostponed] and finishes.
 */
@AndroidEntryPoint
class PostponeActivity : ComponentActivity() {

    @Inject
    lateinit var reminderScheduler: ReminderScheduler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val activityId = intent.getStringExtra(NotificationHelper.EXTRA_ACTIVITY_ID)
        val activityName = intent.getStringExtra(NotificationHelper.EXTRA_ACTIVITY_NAME)
        val className = intent.getStringExtra(NotificationHelper.EXTRA_CLASS_NAME)
        val notifId = intent.getIntExtra(NotificationHelper.EXTRA_NOTIF_ID, -1)

        if (activityId.isNullOrEmpty() || activityName.isNullOrEmpty()) {
            finish()
            return
        }

        setContent {
            PostponeDialog(
                onConfirm = { hour, minute ->
                    val now = LocalTime.now()
                    val picked = LocalTime.of(hour, minute)
                    if (!picked.isAfter(now)) {
                        Toast.makeText(
                            this,
                            "Pick a time later than now today",
                            Toast.LENGTH_SHORT,
                        ).show()
                        return@PostponeDialog
                    }
                    if (notifId != -1) {
                        NotificationManagerCompat.from(this).cancel(notifId)
                    }
                    // schedulePostponed is suspend (does a DB lookup to cancel today's
                    // original alarms before installing the postpone alarm).
                    lifecycleScope.launch {
                        reminderScheduler.schedulePostponed(
                            activityId = activityId,
                            activityName = activityName,
                            className = className,
                            time = picked,
                        )
                        finish()
                    }
                },
                onCancel = { finish() },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PostponeDialog(
    onConfirm: (hour: Int, minute: Int) -> Unit,
    onCancel: () -> Unit,
) {
    val now = LocalTime.now()
    val state = rememberTimePickerState(
        initialHour = now.hour,
        initialMinute = ((now.minute / 5) + 1) * 5 % 60, // round up to next 5-minute mark
        is24Hour = false,
    )
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Postpone to") },
        text = {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                TimePicker(state = state)
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(state.hour, state.minute) }) {
                Text("Set")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) { Text("Cancel") }
        },
    )
}
