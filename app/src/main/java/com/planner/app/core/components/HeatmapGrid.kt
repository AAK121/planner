package com.planner.app.core.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.planner.app.core.theme.HeatmapDark1
import com.planner.app.core.theme.HeatmapDark2
import com.planner.app.core.theme.HeatmapDark3
import com.planner.app.core.theme.HeatmapDark4
import com.planner.app.core.theme.HeatmapLight1
import com.planner.app.core.theme.HeatmapLight2
import com.planner.app.core.theme.HeatmapLight3
import com.planner.app.core.theme.HeatmapLight4
import com.planner.app.core.theme.LocalThemeVariant
import com.planner.app.core.theme.ThemeVariant
import com.planner.app.domain.model.HeatmapCell

@Composable
fun HeatmapGrid(
    cells: List<HeatmapCell>,
    modifier: Modifier = Modifier,
    weeks: Int = 12,
    cellGap: Dp = 3.dp,
) {
    val emptyColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
    val isDark = LocalThemeVariant.current == ThemeVariant.DARK
    val activePalette = if (isDark) listOf(HeatmapDark1, HeatmapDark2, HeatmapDark3, HeatmapDark4)
                        else        listOf(HeatmapLight1, HeatmapLight2, HeatmapLight3, HeatmapLight4)

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val cellSize = (maxWidth - cellGap * (weeks - 1)) / weeks
        val rowHeight = cellSize * 7 + cellGap * 6
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(rowHeight),
        ) {
            val cs = cellSize.toPx()
            val cg = cellGap.toPx()
            cells.take(weeks * 7).forEachIndexed { index, cell ->
                val col = index / 7
                val row = index % 7
                val color = if (cell.intensity == 0) emptyColor
                            else activePalette.getOrElse(cell.intensity - 1) { activePalette[0] }
                drawRoundRect(
                    color = color,
                    topLeft = Offset(col * (cs + cg), row * (cs + cg)),
                    size = Size(cs, cs),
                    cornerRadius = CornerRadius(2.dp.toPx()),
                )
            }
        }
    }
}
