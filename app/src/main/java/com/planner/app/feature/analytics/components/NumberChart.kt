package com.planner.app.feature.analytics.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.planner.app.core.theme.ShapeCard
import com.planner.app.domain.model.ChartSeries

@Composable
fun NumberChart(
    series: ChartSeries,
    modifier: Modifier = Modifier,
) {
    if (series.dataPoints.isEmpty()) return

    val entries = remember(series.dataPoints) {
        series.dataPoints.mapIndexed { i, (_, v) -> FloatEntry(i.toFloat(), v.toFloat()) }
    }
    val model = remember(entries) { entryModelOf(entries) }

    Surface(
        shape = ShapeCard,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = series.label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            Chart(
                chart = lineChart(),
                model = model,
                startAxis = rememberStartAxis(),
                bottomAxis = rememberBottomAxis(),
                modifier = Modifier.height(120.dp),
            )
        }
    }
}
