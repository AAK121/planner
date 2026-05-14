package com.planner.app.core.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.planner.app.core.theme.ShapePill
import com.planner.app.domain.model.LogStatus

@Composable
fun StatusPill(
    status: LogStatus,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val label = when (status) {
        LogStatus.PENDING -> "Pending"
        LogStatus.DONE    -> "Done"
        LogStatus.SKIPPED -> "Skip"
    }

    val containerColor = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
    val contentColor   = if (selected) MaterialTheme.colorScheme.onPrimary
                         else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        shape = ShapePill,
        color = containerColor,
        modifier = modifier
            .then(
                if (!selected) Modifier.border(0.5.dp, MaterialTheme.colorScheme.outline, ShapePill)
                else Modifier
            )
            .clickable { onClick() },
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
        )
    }
}
