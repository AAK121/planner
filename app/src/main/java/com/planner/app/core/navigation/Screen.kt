package com.planner.app.core.navigation

sealed class Screen(val route: String) {
    object Onboarding  : Screen("onboarding")
    object SignIn      : Screen("sign_in")
    object Home        : Screen("home")
    object Dashboard   : Screen("dashboard")
    object Calendar    : Screen("calendar")
    object Settings    : Screen("settings")

    object CreateActivity : Screen("create_activity?activityId={activityId}") {
        fun route(activityId: String? = null) =
            if (activityId != null) "create_activity?activityId=$activityId"
            else "create_activity?activityId="
    }

    object LogEntry : Screen("log_entry/{activityId}") {
        fun route(activityId: String) = "log_entry/$activityId"
    }

    object Analytics : Screen("analytics/{activityId}") {
        fun route(activityId: String) = "analytics/$activityId"
    }
}
