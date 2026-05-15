package com.planner.app.feature.main

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.planner.app.core.components.PlannerBottomBar
import com.planner.app.core.components.PlannerFab
import com.planner.app.core.navigation.Screen
import com.planner.app.feature.activities.ActivitiesContent
import com.planner.app.feature.activities.ActivitiesViewModel
import com.planner.app.feature.calendar.CalendarContent
import com.planner.app.feature.calendar.CalendarViewModel
import com.planner.app.feature.dashboard.DashboardContent
import com.planner.app.feature.dashboard.DashboardViewModel
import com.planner.app.feature.home.HomeContent
import com.planner.app.feature.home.HomeViewModel
import kotlinx.coroutines.launch

private val tabRoutes = listOf(
    Screen.Home.route,
    Screen.Calendar.route,
    Screen.Dashboard.route,
    Screen.Activities.route,
)

@Composable
fun MainScreen(
    onNavigateToLog: (String) -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    onNavigateToAnalytics: (String) -> Unit,
    onNavigateToClass: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
) {
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 4 })
    val scope = rememberCoroutineScope()

    // settledPage only changes when a swipe finishes — so MainScreen itself recomposes
    // at most once per completed tab switch, never on every animation frame.
    BackHandler(enabled = pagerState.settledPage != 0) {
        scope.launch { pagerState.animateScrollToPage(0) }
    }

    // Obtain all ViewModels once — they stay alive for the lifetime of MainScreen.
    val homeVm: HomeViewModel = hiltViewModel()
    val calendarVm: CalendarViewModel = hiltViewModel()
    val dashboardVm: DashboardViewModel = hiltViewModel()
    val activitiesVm: ActivitiesViewModel = hiltViewModel()

    Scaffold(
        topBar = {},
        floatingActionButton = {
            // currentPage is read inside this @Composable lambda slot — only this slot
            // recomposes mid-swipe, not the whole MainScreen.
            val page = pagerState.currentPage
            if (page == 0 || page == 3) PlannerFab(onClick = onNavigateToCreate)
        },
        bottomBar = {
            // Same isolation: only this lambda recomposes when currentPage changes.
            PlannerBottomBar(
                currentRoute = tabRoutes[pagerState.currentPage],
                onNavigate = { screen ->
                    val index = when (screen.route) {
                        Screen.Home.route       -> 0
                        Screen.Calendar.route   -> 1
                        Screen.Dashboard.route  -> 2
                        Screen.Activities.route -> 3
                        else -> 0
                    }
                    scope.launch { pagerState.animateScrollToPage(index) }
                },
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            beyondViewportPageCount = 1,
            userScrollEnabled = true,
        ) { page ->
            when (page) {
                0 -> HomeContent(
                    innerPadding = PaddingValues(),
                    onNavigateToLog = onNavigateToLog,
                    onNavigateToCreate = onNavigateToCreate,
                    onNavigateToAnalytics = onNavigateToAnalytics,
                    onNavigateToSettings = onNavigateToSettings,
                    viewModel = homeVm,
                )
                1 -> CalendarContent(
                    innerPadding = PaddingValues(),
                    onLogActivity = onNavigateToLog,
                    onNavigateToSettings = onNavigateToSettings,
                    viewModel = calendarVm,
                )
                2 -> DashboardContent(
                    innerPadding = PaddingValues(),
                    onNavigateToAnalytics = onNavigateToAnalytics,
                    onNavigateToClass = onNavigateToClass,
                    onNavigateToSettings = onNavigateToSettings,
                    viewModel = dashboardVm,
                )
                3 -> ActivitiesContent(
                    innerPadding = PaddingValues(),
                    onEditActivity = onNavigateToEdit,
                    onNavigateToSettings = onNavigateToSettings,
                    viewModel = activitiesVm,
                )
            }
        }
    }
}
