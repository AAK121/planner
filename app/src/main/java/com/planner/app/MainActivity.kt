package com.planner.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.planner.app.core.navigation.PlannerNavGraph
import com.planner.app.core.navigation.Screen
import com.planner.app.core.theme.PlannerTheme
import com.planner.app.data.local.datastore.PreferencesDataStore
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var prefs: PreferencesDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeVariant by prefs.themeVariant.collectAsState(initial = com.planner.app.core.theme.ThemeVariant.LIGHT)
            val onboardingDone by prefs.onboardingDone.collectAsState(initial = false)

            PlannerTheme(variant = themeVariant) {
                val navController = rememberNavController()
                PlannerNavGraph(
                    navController = navController,
                    startDestination = if (onboardingDone) Screen.Home.route else Screen.Onboarding.route,
                )
            }
        }
    }
}
