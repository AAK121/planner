package com.planner.app.feature.home

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.planner.app.core.components.*
import com.planner.app.core.theme.serifGreeting
import com.planner.app.core.utils.DateUtils.toDisplayString
import com.planner.app.domain.model.LogStatus

@Composable
fun HomeScreen(
    onNavigateToLog: (String) -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToAnalytics: (String) -> Unit,
    onNavigateToDashboard: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        floatingActionButton = {
            PlannerFab(onClick = onNavigateToCreate)
        },
        bottomBar = {
            PlannerBottomBar(
                currentRoute = "home",
                onNavigate = { screen ->
                    when (screen.route) {
                        "dashboard" -> onNavigateToDashboard()
                        "calendar"  -> onNavigateToCalendar()
                        "settings"  -> onNavigateToSettings()
                        else        -> {}
                    }
                },
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        LazyColumn(
            contentPadding = innerPadding,
            modifier = Modifier.fillMaxSize(),
        ) {
            // Header
            item {
                HomeHeader(
                    dateLabel = state.today.toDisplayString(),
                    progressFraction = state.progressFraction,
                    doneCount = state.doneCount,
                    totalCount = state.totalCount,
                    onSettingsClick = onNavigateToSettings,
                )
            }

            val pending = state.activities.filter { activity ->
                state.logs[activity.id]?.status != LogStatus.DONE
            }
            val done = state.activities.filter { activity ->
                state.logs[activity.id]?.status == LogStatus.DONE
            }

            if (pending.isNotEmpty()) {
                item { SectionLabel(text = "Today") }
            }

            itemsIndexed(pending) { _, activity ->
                ActivityItem(
                    activity = activity,
                    log = state.logs[activity.id],
                    streak = state.streaks[activity.id] ?: 0,
                    onToggleDone = { viewModel.toggleDone(activity) },
                    onItemClick = { onNavigateToLog(activity.id) },
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 28.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                )
            }

            if (done.isNotEmpty()) {
                item { SectionLabel(text = "Completed") }
                itemsIndexed(done) { _, activity ->
                    ActivityItem(
                        activity = activity,
                        log = state.logs[activity.id],
                        streak = state.streaks[activity.id] ?: 0,
                        onToggleDone = { viewModel.toggleDone(activity) },
                        onItemClick = { onNavigateToAnalytics(activity.id) },
                    )
                }
            }

            if (state.activities.isEmpty() && !state.isLoading) {
                item {
                    EmptyHome(onAddActivity = onNavigateToCreate)
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun HomeHeader(
    dateLabel: String,
    progressFraction: Float,
    doneCount: Int,
    totalCount: Int,
    onSettingsClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 28.dp, end = 20.dp, top = 24.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Good morning",
                style = serifGreeting,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = dateLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        ProgressRing(
            fraction = progressFraction,
            size = 52.dp,
            strokeWidth = 5.dp,
            label = "$doneCount/$totalCount",
        )

        Spacer(Modifier.width(8.dp))

        IconButton(onClick = onSettingsClick) {
            Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = "Settings",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun EmptyHome(onAddActivity: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("📋", style = MaterialTheme.typography.displayMedium)
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Nothing scheduled today",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Add an activity to get started.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onAddActivity) {
            Text("Add activity")
        }
    }
}
