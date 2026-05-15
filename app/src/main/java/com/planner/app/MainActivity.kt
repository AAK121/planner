package com.planner.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
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

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            // User granted or denied — no app-side action; notifications will simply
            // be suppressed by the system if denied.
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        maybeRequestNotificationPermission()
        setContent {
            val themeVariant by prefs.themeVariant.collectAsState(initial = com.planner.app.core.theme.ThemeVariant.LIGHT)
            val onboardingDone by prefs.onboardingDone.collectAsState(initial = null)

            PlannerTheme(variant = themeVariant) {
                val navController = rememberNavController()
                onboardingDone?.let { done ->
                    PlannerNavGraph(
                        navController = navController,
                        startDestination = if (done) Screen.Home.route else Screen.Onboarding.route,
                    )
                }
            }
        }
    }

    private fun maybeRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
