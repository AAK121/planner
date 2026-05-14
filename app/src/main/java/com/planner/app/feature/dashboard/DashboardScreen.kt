package com.planner.app.feature.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.planner.app.core.components.PlannerBottomBar
import com.planner.app.core.components.PlannerTopBar
import com.planner.app.core.components.SectionLabel
import com.planner.app.core.navigation.Screen
import com.planner.app.core.theme.ShapeCard
import com.planner.app.feature.dashboard.components.CorrelationCard
import com.planner.app.feature.dashboard.components.TimeAllocationPie

@Composable
fun DashboardScreen(
    onBack: () -> Unit,
    onActivityClick: (String) -> Unit,
    onNavigateToTab: (Screen) -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { PlannerTopBar(title = "Stats", onBack = onBack) },
        bottomBar = {
            PlannerBottomBar(
                currentRoute = Screen.Dashboard.route,
                onNavigate = onNavigateToTab,
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

        LazyColumn(contentPadding = innerPadding) {

            // Momentum score
            item {
                Surface(
                    shape = ShapeCard,
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 28.dp, vertical = 16.dp),
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Weekly momentum",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "${state.momentumScore}",
                            style = MaterialTheme.typography.displayMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { state.momentumScore / 100f },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

            // Time allocation
            if (state.timeAllocations.isNotEmpty()) {
                item {
                    SectionLabel(text = "Time allocation (30 days)")
                    TimeAllocationPie(allocations = state.timeAllocations)
                    Spacer(Modifier.height(20.dp))
                }
            }

            // Correlations
            if (state.correlations.isNotEmpty()) {
                item { SectionLabel(text = "Activity correlations") }
                state.correlations.take(5).forEach { correlation ->
                    item {
                        CorrelationCard(
                            correlation = correlation,
                            modifier = Modifier.padding(horizontal = 28.dp),
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }

            // Weekly insight
            state.weeklyInsight?.let { insight ->
                item {
                    SectionLabel(text = "Weekly insight")
                    Surface(
                        shape = ShapeCard,
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 1.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 28.dp),
                    ) {
                        Text(
                            text = insight,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun DashboardContent(
    innerPadding: PaddingValues,
    onActivityClick: (String) -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    if (state.isLoading) {
        Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    LazyColumn(contentPadding = innerPadding) {
        item {
            Surface(
                shape = ShapeCard,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp, vertical = 16.dp),
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Weekly momentum", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    Text("${state.momentumScore}", style = MaterialTheme.typography.displayMedium, color = MaterialTheme.colorScheme.onBackground)
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(progress = { state.momentumScore / 100f }, modifier = Modifier.fillMaxWidth())
                }
            }
        }

        if (state.timeAllocations.isNotEmpty()) {
            item {
                SectionLabel(text = "Time allocation (30 days)")
                TimeAllocationPie(allocations = state.timeAllocations)
                Spacer(Modifier.height(20.dp))
            }
        }

        if (state.correlations.isNotEmpty()) {
            item { SectionLabel(text = "Activity correlations") }
            state.correlations.take(5).forEach { correlation ->
                item {
                    CorrelationCard(correlation = correlation, modifier = Modifier.padding(horizontal = 28.dp))
                    Spacer(Modifier.height(8.dp))
                }
            }
        }

        state.weeklyInsight?.let { insight ->
            item {
                SectionLabel(text = "Weekly insight")
                Surface(
                    shape = ShapeCard,
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp),
                ) {
                    Text(text = insight, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(16.dp))
                }
            }
        }

        item { Spacer(Modifier.height(80.dp)) }
    }
}
