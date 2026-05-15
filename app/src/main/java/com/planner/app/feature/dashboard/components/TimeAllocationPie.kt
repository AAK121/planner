package com.planner.app.feature.dashboard.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.planner.app.core.theme.*
import com.planner.app.feature.dashboard.AllocationItem
import kotlin.math.atan2
import kotlin.math.sqrt

private val sliceColors = listOf(
    ColorfulTeal, ColorfulCoral, ColorfulAmber, ColorfulSage,
    Color(0xFF9B8EAF), Color(0xFF7A9E9F), Color(0xFFD4816B),
)

@Composable
fun TimeAllocationPie(
    allocations: List<AllocationItem>,
    onActivityClick: (String) -> Unit,
    onClassClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (allocations.isEmpty()) return

    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    Column(modifier = modifier.padding(horizontal = 28.dp)) {
        Box(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(
                modifier = Modifier
                    .size(200.dp)
                    .pointerInput(allocations) {
                        detectTapGestures { offset ->
                            val centerX = size.width / 2f
                            val centerY = size.height / 2f
                            val dx = offset.x - centerX
                            val dy = offset.y - centerY
                            val dist = sqrt(dx * dx + dy * dy)

                            if (dist > size.width / 2f) {
                                selectedIndex = null
                                return@detectTapGestures
                            }

                            var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                            angle = (angle + 90f + 360f) % 360f

                            var cumulative = 0f
                            var tapped: Int? = null
                            allocations.forEachIndexed { idx, item ->
                                val sweep = 360f * item.fraction
                                if (tapped == null && angle >= cumulative && angle < cumulative + sweep) {
                                    tapped = idx
                                }
                                cumulative += sweep
                            }

                            val found = tapped
                            selectedIndex = if (found != null && selectedIndex != found) found else null
                        }
                    },
            ) {
                var startAngle = -90f
                allocations.forEachIndexed { index, item ->
                    val sweep = 360f * item.fraction
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

            selectedIndex?.let { idx ->
                val item = allocations[idx]
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.inverseSurface,
                    ),
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.inverseOnSurface,
                        )
                        Text(
                            text = "${(item.fraction * 100).toInt()}%",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.inverseOnSurface,
                        )
                    }
                }
            }
        }

        val trackable = remember(allocations) { allocations.filter { it.trackAnalytics } }
        val colorIndexByItem = remember(allocations) {
            allocations.withIndex().associate { (i, item) -> item to i }
        }
        if (trackable.isNotEmpty()) {
            Spacer(Modifier.height(20.dp))
            Text(
                text = "Tap an activity for details",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(4.dp))
            trackable.forEach { item ->
                val colorIndex = colorIndexByItem[item] ?: 0
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (item.isClass) onClassClick(item.id) else onActivityClick(item.id)
                        }
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(sliceColors[colorIndex % sliceColors.size]),
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = "${(item.fraction * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
