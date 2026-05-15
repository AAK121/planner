package com.planner.app.feature.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.outlined.Settings
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
                    TimeAllocationPie(
                        allocations = state.timeAllocations,
                        onActivityClick = onActivityClick,
                        onClassClick = onActivityClick,  // standalone screen has no class route — fall back to activity nav
                    )
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
    onNavigateToAnalytics: (String) -> Unit,
    onNavigateToClass: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val period by viewModel.period.collectAsState()
    var showPeriodMenu by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        PlannerTopBar(
            title = "Stats",
            windowInsets = WindowInsets(0),
            actions = {
                IconButton(onClick = onNavigateToSettings) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
                }
            },
        )

        // Period selector
        Box(modifier = Modifier.padding(start = 20.dp)) {
            TextButton(onClick = { showPeriodMenu = true }) {
                Text(period.label, style = MaterialTheme.typography.bodyMedium)
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
            }
            DropdownMenu(
                expanded = showPeriodMenu,
                onDismissRequest = { showPeriodMenu = false },
            ) {
                StatsPeriod.entries.forEach { p ->
                    DropdownMenuItem(
                        text = { Text(p.label) },
                        onClick = {
                            viewModel.setPeriod(p)
                            showPeriodMenu = false
                        },
                    )
                }
            }
        }

        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Column
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
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

            if (state.timeAllocations.isEmpty() && state.correlations.isEmpty()) {
                item {
                    Text(
                        text = "Keep logging activities — charts appear after a few days of data.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 28.dp, vertical = 12.dp),
                    )
                }
            }

            if (state.timeAllocations.isNotEmpty()) {
                item {
                    SectionLabel(text = "Activity breakdown (${period.label})")
                    TimeAllocationPie(
                        allocations = state.timeAllocations,
                        onActivityClick = onNavigateToAnalytics,
                        onClassClick = onNavigateToClass,
                    )
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
}
