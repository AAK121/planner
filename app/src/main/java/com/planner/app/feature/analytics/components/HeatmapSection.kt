package com.planner.app.feature.analytics.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.planner.app.core.components.HeatmapGrid
import com.planner.app.core.components.SectionLabel
import com.planner.app.core.theme.ShapeCard
import com.planner.app.domain.model.HeatmapCell

@Composable
fun HeatmapSection(cells: List<HeatmapCell>, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        SectionLabel(text = "Activity (last 12 weeks)")
        Surface(
            shape = ShapeCard,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp),
        ) {
            HeatmapGrid(cells = cells, modifier = Modifier.padding(16.dp))
        }
    }
}
