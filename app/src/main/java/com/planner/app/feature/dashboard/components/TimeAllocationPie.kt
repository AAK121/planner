package com.planner.app.feature.dashboard.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.planner.app.core.theme.*

private val sliceColors = listOf(
    ColorfulTeal, ColorfulCoral, ColorfulAmber, ColorfulSage,
    Color(0xFF9B8EAF), Color(0xFF7A9E9F), Color(0xFFD4816B),
)

@Composable
fun TimeAllocationPie(
    allocations: List<Pair<String, Float>>,
    modifier: Modifier = Modifier,
) {
    if (allocations.isEmpty()) return

    Column(modifier = modifier.padding(horizontal = 28.dp)) {
        Canvas(modifier = Modifier.size(160.dp).align(Alignment.CenterHorizontally)) {
            var startAngle = -90f
            allocations.forEachIndexed { index, (_, fraction) ->
                val sweep = 360f * fraction
                drawArc(
                    color = sliceColors[index % sliceColors.size],
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = true,
                    topLeft = Offset(0f, 0f),
                    size = Size(size.width, size.height),
                )
                startAngle += sweep
            }
        }

        Spacer(Modifier.height(16.dp))

        allocations.forEachIndexed { index, (name, fraction) ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 2.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .then(Modifier),
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(sliceColors[index % sliceColors.size])
                    }
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "${(fraction * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
