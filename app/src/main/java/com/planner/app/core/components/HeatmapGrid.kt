package com.planner.app.core.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.planner.app.core.theme.HeatmapDark0
import com.planner.app.core.theme.HeatmapDark1
import com.planner.app.core.theme.HeatmapDark2
import com.planner.app.core.theme.HeatmapDark3
import com.planner.app.core.theme.HeatmapDark4
import com.planner.app.core.theme.HeatmapLight0
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
    cellSize: Dp = 12.dp,
    cellGap: Dp = 3.dp,
    weeks: Int = 52,
) {
    val isDark = LocalThemeVariant.current == ThemeVariant.DARK
    val palette = if (isDark) listOf(HeatmapDark0, HeatmapDark1, HeatmapDark2, HeatmapDark3, HeatmapDark4)
                  else        listOf(HeatmapLight0, HeatmapLight1, HeatmapLight2, HeatmapLight3, HeatmapLight4)

    val totalHeight = (cellSize + cellGap) * 7 + cellGap
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(totalHeight),
    ) {
        val cs = cellSize.toPx()
        val cg = cellGap.toPx()

        cells.take(weeks * 7).forEachIndexed { index, cell ->
            val col = index / 7
            val row = index % 7
            val x = col * (cs + cg)
            val y = row * (cs + cg)
            val color = palette.getOrElse(cell.intensity) { palette[0] }
            drawRoundRect(
                color = color,
                topLeft = Offset(x, y),
                size = Size(cs, cs),
                cornerRadius = CornerRadius(2.dp.toPx()),
            )
        }
    }
}
