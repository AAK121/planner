package com.planner.app.feature.dashboard.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.planner.app.core.theme.ShapeCard
import com.planner.app.domain.model.ActivityCorrelation

@Composable
fun CorrelationCard(correlation: ActivityCorrelation, modifier: Modifier = Modifier) {
    Surface(
        shape = ShapeCard,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Text("🔗", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = correlation.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Score: ${(correlation.correlationScore * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
