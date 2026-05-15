package com.planner.app.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.planner.app.feature.analytics.AnalyticsScreen
import com.planner.app.feature.class_detail.ClassDetailScreen
import com.planner.app.feature.create_activity.CreateActivityScreen
import com.planner.app.feature.log_entry.LogEntryScreen
import com.planner.app.feature.main.MainScreen
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
                onDone = {
                    navController.navigate(Screen.SignIn.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.SignIn.route) {
            SignInScreen(
                onSignedIn = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.SignIn.route) { inclusive = true }
                    }
                }
            )
        }

        // All main tabs (Home, Calendar, Stats, Activities) live inside MainScreen
        composable(Screen.Home.route) {
            MainScreen(
                onNavigateToLog       = { id -> navController.navigate(Screen.LogEntry.route(id)) },
                onNavigateToCreate    = { navController.navigate(Screen.CreateActivity.route()) },
                onNavigateToEdit      = { id -> navController.navigate(Screen.CreateActivity.route(id)) },
                onNavigateToAnalytics = { id -> navController.navigate(Screen.Analytics.route(id)) },
                onNavigateToClass     = { name -> navController.navigate(Screen.ClassDetail.route(name)) },
                onNavigateToSettings  = { navController.navigate(Screen.Settings.route) },
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
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

        composable(
            route = Screen.ClassDetail.route,
            arguments = listOf(navArgument("className") { type = NavType.StringType })
        ) {
            ClassDetailScreen(
                onBack = { navController.popBackStack() },
                onActivityClick = { id -> navController.navigate(Screen.Analytics.route(id)) },
            )
        }
    }
}
