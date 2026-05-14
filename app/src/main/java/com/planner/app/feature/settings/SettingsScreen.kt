package com.planner.app.feature.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.planner.app.core.components.PlannerTopBar
import com.planner.app.core.components.SectionLabel
import com.planner.app.core.theme.ShapeInput
import com.planner.app.core.theme.ThemeVariant
import kotlinx.coroutines.delay

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    Scaffold(
        topBar = { PlannerTopBar(title = "Settings", onBack = onBack) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        SettingsContent(innerPadding = innerPadding, viewModel = viewModel)
    }
}

@Composable
fun SettingsContent(
    innerPadding: PaddingValues,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    // Local state for username — updates immediately while typing, persists after 400ms pause
    var usernameInput by remember(state.username) { mutableStateOf(state.username) }
    LaunchedEffect(usernameInput) {
        delay(400)
        viewModel.setUsername(usernameInput)
    }

    LazyColumn(contentPadding = innerPadding, modifier = Modifier.fillMaxSize()) {

        item { SectionLabel(text = "Profile") }
        item {
            Column(modifier = Modifier.padding(horizontal = 28.dp)) {
                OutlinedTextField(
                    value = usernameInput,
                    onValueChange = { usernameInput = it },
                    label = { Text("Your name") },
                    placeholder = { Text("Enter your name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = ShapeInput,
                    singleLine = true,
                )
            }
            Spacer(Modifier.height(20.dp))
        }

        item { SectionLabel(text = "Appearance") }
        item {
            Column(modifier = Modifier.padding(horizontal = 28.dp)) {
                Text("Theme", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThemeVariant.entries.forEach { variant ->
                        FilterChip(
                            selected = state.themeVariant == variant,
                            onClick = { viewModel.setTheme(variant) },
                            label = {
                                Text(
                                    text = variant.name.lowercase().replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            },
                        )
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
        }

        item { SectionLabel(text = "Notifications") }
        item {
            Column(modifier = Modifier.padding(horizontal = 28.dp)) {
                listOf(
                    "Reminders"    to Pair(state.notifReminders, viewModel::setNotifReminders),
                    "Streaks"      to Pair(state.notifStreaks, viewModel::setNotifStreaks),
                    "Insights"     to Pair(state.notifInsights, viewModel::setNotifInsights),
                    "Celebrations" to Pair(state.notifCelebrations, viewModel::setNotifCelebrations),
                ).forEach { (label, pair) ->
                    val (enabled, setter) = pair
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(label, style = MaterialTheme.typography.bodyLarge)
                        Switch(checked = enabled, onCheckedChange = setter)
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
        }

        item { SectionLabel(text = "AI & Insights") }
        item {
            Column(modifier = Modifier.padding(horizontal = 28.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("On-device AI", style = MaterialTheme.typography.bodyLarge)
                        Text("Gemma 2B — runs offline", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = state.llmOnDeviceEnabled, onCheckedChange = viewModel::setLlmOnDevice)
                }
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Cloud AI", style = MaterialTheme.typography.bodyLarge)
                        Text("Richer insights using your own API key", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = state.llmCloudEnabled, onCheckedChange = viewModel::setLlmCloud)
                }
                if (state.llmCloudEnabled) {
                    Spacer(Modifier.height(16.dp))
                    Text("Provider", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("anthropic", "openai", "gemini").forEach { provider ->
                            FilterChip(
                                selected = state.llmProvider == provider,
                                onClick = { viewModel.setLlmProvider(provider) },
                                label = { Text(provider, style = MaterialTheme.typography.bodySmall) },
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = state.llmApiKey,
                        onValueChange = viewModel::setLlmApiKey,
                        label = { Text("API Key") },
                        placeholder = { Text("sk-…") },
                        visualTransformation = PasswordVisualTransformation(),
                        shape = ShapeInput,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                }
            }
            Spacer(Modifier.height(80.dp))
        }
    }
}
