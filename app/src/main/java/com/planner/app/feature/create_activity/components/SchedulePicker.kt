package com.planner.app.feature.create_activity.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.planner.app.core.theme.ShapeCircle
import com.planner.app.core.theme.ShapeInput

@Composable
fun SchedulePicker(
    selectedDays: List<Int>,
    onToggleDay: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val labels = listOf("M", "T", "W", "T", "F", "S", "S")

    Column(modifier = modifier) {
        Text(
            text = "Schedule",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            labels.forEachIndexed { index, label ->
                val dayValue = index + 1
                val selected = selectedDays.contains(dayValue)
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(ShapeCircle)
                        .then(
                            if (selected) Modifier
                            else Modifier.border(0.5.dp, MaterialTheme.colorScheme.outline, ShapeCircle)
                        )
                        .then(
                            if (selected) Modifier.clickable { onToggleDay(dayValue) }
                            else Modifier.clickable { onToggleDay(dayValue) }
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Surface(
                        shape = ShapeCircle,
                        color = if (selected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (selected) MaterialTheme.colorScheme.onPrimary
                                        else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}
