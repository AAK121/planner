package com.planner.app.feature.analytics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.planner.app.core.components.ComplianceBar
import com.planner.app.core.components.PlannerTopBar
import com.planner.app.core.components.SectionLabel
import com.planner.app.core.theme.ShapeCard
import com.planner.app.core.theme.StreakOrange
import com.planner.app.core.utils.toPercent
import com.planner.app.feature.analytics.components.HeatmapSection
import com.planner.app.feature.analytics.components.NumberChart
import com.planner.app.feature.analytics.components.PatternCard

@Composable
fun AnalyticsScreen(
    onBack: () -> Unit,
    onEditActivity: (String) -> Unit,
    viewModel: AnalyticsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            PlannerTopBar(
                title = state.activity?.name ?: "Analytics",
                onBack = onBack,
                actions = {
                    state.activity?.let { activity ->
                        IconButton(onClick = { onEditActivity(activity.id) }) {
                            Icon(Icons.Outlined.Edit, contentDescription = "Edit")
                        }
                    }
                },
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val data = state.analyticsData ?: return@Scaffold
        LazyColumn(contentPadding = innerPadding) {

            // Streak cards
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 28.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    StatCard(
                        label = "Current streak",
                        value = "🔥 ${data.currentStreak}",
                        modifier = Modifier.weight(1f),
                    )
                    StatCard(
                        label = "Longest streak",
                        value = "⭐ ${data.longestStreak}",
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            // Compliance
            item {
                SectionLabel(text = "Compliance")
                Column(
                    modifier = Modifier.padding(horizontal = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    listOf(
                        "7 days" to data.complianceRate7d,
                        "30 days" to data.complianceRate30d,
                        "90 days" to data.complianceRate90d,
                        "All time" to data.complianceRateAll,
                    ).forEach { (period, rate) ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = period,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.width(70.dp),
                            )
                            ComplianceBar(fraction = rate, modifier = Modifier.weight(1f))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = rate.toPercent(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.width(36.dp),
                            )
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
            }

            // Heatmap
            item {
                HeatmapSection(cells = data.heatmapCells)
                Spacer(Modifier.height(20.dp))
            }

            // Charts
            if (data.chartSeries.isNotEmpty()) {
                item { SectionLabel(text = "Progress") }
                data.chartSeries.forEach { series ->
                    item {
                        NumberChart(series = series)
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }

            // Patterns
            if (data.missedDayPatterns.isNotEmpty()) {
                item { SectionLabel(text = "Patterns") }
                data.missedDayPatterns.forEach { pattern ->
                    val dayLabel = listOf("Mon","Tue","Wed","Thu","Fri","Sat","Sun")[pattern.dayOfWeek - 1]
                    item {
                        PatternCard(
                            text = "You skip $dayLabel ${(pattern.missRate * 100).toInt()}% of the time",
                            modifier = Modifier.padding(horizontal = 28.dp),
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        shape = ShapeCard,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = modifier,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}
