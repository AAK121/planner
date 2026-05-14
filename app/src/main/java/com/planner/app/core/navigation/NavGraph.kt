package com.planner.app.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.planner.app.feature.analytics.AnalyticsScreen
import com.planner.app.feature.calendar.CalendarScreen
import com.planner.app.feature.create_activity.CreateActivityScreen
import com.planner.app.feature.dashboard.DashboardScreen
import com.planner.app.feature.home.HomeScreen
import com.planner.app.feature.log_entry.LogEntryScreen
import com.planner.app.feature.onboarding.OnboardingScreen
import com.planner.app.feature.settings.SettingsScreen
import com.planner.app.feature.signin.SignInScreen

@Composable
fun PlannerNavGraph(
    navController: NavHostController,
    startDestination: String,
) {
    NavHost(navController = navController, startDestination = startDestination) {

        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onDone = { navController.navigate(Screen.SignIn.route) {
                    popUpTo(Screen.Onboarding.route) { inclusive = true }
                }}
            )
        }

        composable(Screen.SignIn.route) {
            SignInScreen(
                onSignedIn = { navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.SignIn.route) { inclusive = true }
                }}
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToLog    = { id -> navController.navigate(Screen.LogEntry.route(id)) },
                onNavigateToCreate = { navController.navigate(Screen.CreateActivity.route()) },
                onNavigateToAnalytics = { id -> navController.navigate(Screen.Analytics.route(id)) },
                onNavigateToDashboard = { navController.navigate(Screen.Dashboard.route) },
                onNavigateToCalendar  = { navController.navigate(Screen.Calendar.route) },
                onNavigateToSettings  = { navController.navigate(Screen.Settings.route) },
            )
        }

        composable(
            route = Screen.CreateActivity.route,
            arguments = listOf(navArgument("activityId") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) {
            CreateActivityScreen(
                onDone = { navController.popBackStack() },
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = Screen.LogEntry.route,
            arguments = listOf(navArgument("activityId") { type = NavType.StringType })
        ) {
            LogEntryScreen(
                onDone = { navController.popBackStack() },
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = Screen.Analytics.route,
            arguments = listOf(navArgument("activityId") { type = NavType.StringType })
        ) {
            AnalyticsScreen(
                onBack = { navController.popBackStack() },
                onEditActivity = { id -> navController.navigate(Screen.CreateActivity.route(id)) },
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onBack = { navController.popBackStack() },
                onActivityClick = { id -> navController.navigate(Screen.Analytics.route(id)) },
            )
        }

        composable(Screen.Calendar.route) {
            CalendarScreen(
                onBack = { navController.popBackStack() },
                onLogActivity = { id -> navController.navigate(Screen.LogEntry.route(id)) },
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
