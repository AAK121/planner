package com.planner.app.feature.main

import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.hilt.navigation.compose.hiltViewModel
import com.planner.app.core.components.PlannerBottomBar
import com.planner.app.core.components.PlannerFab
import com.planner.app.core.components.PlannerTopBar
import com.planner.app.core.navigation.Screen
import com.planner.app.feature.activities.ActivitiesContent
import com.planner.app.feature.activities.ActivitiesViewModel
import com.planner.app.feature.calendar.CalendarContent
import com.planner.app.feature.calendar.CalendarViewModel
import com.planner.app.feature.dashboard.DashboardContent
import com.planner.app.feature.dashboard.DashboardViewModel
import com.planner.app.feature.home.HomeContent
import com.planner.app.feature.home.HomeViewModel

@Composable
fun MainScreen(
    onNavigateToLog: (String) -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    onNavigateToAnalytics: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    // Back swipe on any non-Home tab → go to Home first
    BackHandler(enabled = selectedTab != 0) {
        selectedTab = 0
    }

    val tabRoutes = listOf(
        Screen.Home.route,
        Screen.Calendar.route,
        Screen.Dashboard.route,
        Screen.Activities.route,
    )

    // Obtain all ViewModels once — they stay alive for the lifetime of MainScreen
    val homeVm: HomeViewModel = hiltViewModel()
    val calendarVm: CalendarViewModel = hiltViewModel()
    val dashboardVm: DashboardViewModel = hiltViewModel()
    val activitiesVm: ActivitiesViewModel = hiltViewModel()

    val settingsAction: @Composable () -> Unit = {
        IconButton(onClick = onNavigateToSettings) {
            Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = "Settings",
                tint = MaterialTheme.colorScheme.onBackground,
            )
        }
    }

    Scaffold(
        topBar = {
            when (selectedTab) {
                1 -> PlannerTopBar(title = "Calendar", actions = { settingsAction() })
                2 -> PlannerTopBar(title = "Stats", actions = { settingsAction() })
                3 -> PlannerTopBar(title = "Activities", actions = { settingsAction() })
                else -> {}
            }
        },
        floatingActionButton = {
            if (selectedTab == 0 || selectedTab == 3) PlannerFab(onClick = onNavigateToCreate)
        },
        bottomBar = {
            PlannerBottomBar(
                currentRoute = tabRoutes[selectedTab],
                onNavigate = { screen ->
                    selectedTab = when (screen.route) {
                        Screen.Home.route       -> 0
                        Screen.Calendar.route   -> 1
                        Screen.Dashboard.route  -> 2
                        Screen.Activities.route -> 3
                        else -> 0
                    }
                },
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        when (selectedTab) {
            0 -> HomeContent(
                innerPadding = innerPadding,
                onNavigateToLog = onNavigateToLog,
                onNavigateToCreate = onNavigateToCreate,
                onNavigateToAnalytics = onNavigateToAnalytics,
                onNavigateToSettings = onNavigateToSettings,
                viewModel = homeVm,
            )
            1 -> CalendarContent(
                innerPadding = innerPadding,
                onLogActivity = onNavigateToLog,
                viewModel = calendarVm,
            )
            2 -> DashboardContent(
                innerPadding = innerPadding,
                onActivityClick = onNavigateToAnalytics,
                viewModel = dashboardVm,
            )
            3 -> ActivitiesContent(
                innerPadding = innerPadding,
                onEditActivity = onNavigateToEdit,
                viewModel = activitiesVm,
            )
        }
    }
}
