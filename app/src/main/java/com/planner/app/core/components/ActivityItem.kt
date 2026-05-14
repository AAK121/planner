package com.planner.app.core.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.planner.app.core.theme.Neutral200
import com.planner.app.core.theme.StreakOrange
import com.planner.app.domain.model.Activity
import com.planner.app.domain.model.ActivityLog
import com.planner.app.domain.model.LogStatus

@Composable
fun ActivityItem(
    activity: Activity,
    log: ActivityLog?,
    streak: Int,
    onToggleDone: () -> Unit,
    onItemClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDone = log?.status == LogStatus.DONE
    val scale by animateFloatAsState(
        targetValue = 1f,
        label = "ItemScale",
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable { onItemClick() }
            .padding(horizontal = 28.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CheckCircle(
            checked = isDone,
            onToggle = onToggleDone,
            activeColor = try {
                Color(android.graphics.Color.parseColor(activity.color))
            } catch (_: Exception) {
                MaterialTheme.colorScheme.primary
            },
        )

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = activity.name,
                style = MaterialTheme.typography.titleMedium,
                color = if (isDone) MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.onBackground,
                textDecoration = if (isDone) TextDecoration.LineThrough else null,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (streak > 0) {
                    Text(
                        text = "🔥 $streak",
                        style = MaterialTheme.typography.bodySmall,
                        color = StreakOrange,
                    )
                    Text(
                        text = "  •  ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                val scheduleLabel = activity.schedule.daysOfWeek
                    .takeIf { it.isNotEmpty() }
                    ?.let { days ->
                        val labels = listOf("M","T","W","T","F","S","S")
                        days.joinToString("") { labels[it - 1] }
                    } ?: activity.type.name.lowercase().replaceFirstChar { it.uppercase() }
                Text(
                    text = scheduleLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
        }

        val timeLabel = activity.schedule.timesOfDay.firstOrNull()
        if (timeLabel != null) {
            Spacer(Modifier.width(8.dp))
            Text(
                text = timeLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(Modifier.width(4.dp))
        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = Neutral200,
            modifier = Modifier.size(16.dp),
        )
    }
}
