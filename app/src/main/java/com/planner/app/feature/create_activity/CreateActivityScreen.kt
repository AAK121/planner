package com.planner.app.feature.create_activity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.planner.app.core.components.PlannerTopBar
import com.planner.app.core.theme.ShapeInput
import com.planner.app.core.theme.ShapePill
import com.planner.app.domain.model.ActivityType
import com.planner.app.feature.create_activity.components.SchedulePicker
import com.planner.app.feature.create_activity.components.TemplatePicker
import com.planner.app.feature.create_activity.components.VariableTreeEditor

private val steps = listOf("Basics", "Schedule", "Tracking", "Goal")

@Composable
fun CreateActivityScreen(
    onDone: () -> Unit,
    onBack: () -> Unit,
    viewModel: CreateActivityViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.savedSuccessfully) {
        if (state.savedSuccessfully) onDone()
    }

    Scaffold(
        topBar = {
            PlannerTopBar(
                title = if (state.isEditing) "Edit activity" else "New activity",
                onBack = if (state.currentStep == 0) onBack else viewModel::prevStep,
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 28.dp),
        ) {
            // Step indicator
            StepIndicator(currentStep = state.currentStep, steps = steps)

            Spacer(Modifier.height(24.dp))

            Box(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                when (state.currentStep) {
                    0 -> BasicsStep(
                        name = state.name,
                        type = state.type,
                        emoji = state.emoji,
                        onNameChange = viewModel::setName,
                        onTypeChange = viewModel::setType,
                        onEmojiChange = viewModel::setEmoji,
                        onSelectTemplate = { template ->
                            viewModel.setName(template.name)
                            viewModel.setEmoji(template.emoji)
                            viewModel.setColor(template.color)
                            viewModel.setType(template.type)
                            viewModel.setVariableTree(template.tree)
                            template.defaultDays.forEach { viewModel.toggleDay(it) }
                        },
                    )
                    1 -> ScheduleStep(
                        selectedDays = state.selectedDays,
                        onToggleDay = viewModel::toggleDay,
                    )
                    2 -> TrackingStep(
                        variableTree = state.variableTree,
                        onTreeChange = viewModel::setVariableTree,
                    )
                    3 -> GoalStep(
                        goal = state.goal,
                        onGoalChange = viewModel::setGoal,
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Next / Save button
            Button(
                onClick = {
                    if (state.currentStep < steps.size - 1) viewModel.nextStep()
                    else viewModel.save()
                },
                shape = ShapePill,
                enabled = state.name.isNotBlank() && !state.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .padding(bottom = 16.dp),
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text(
                        text = if (state.currentStep < steps.size - 1) "Continue" else "Save",
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun StepIndicator(currentStep: Int, steps: List<String>) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        steps.forEachIndexed { index, label ->
            val active = index == currentStep
            val done   = index < currentStep
            FilterChip(
                selected = active || done,
                onClick = {},
                enabled = false,
                label = {
                    Text(label, style = MaterialTheme.typography.labelMedium)
                },
            )
        }
    }
}

@Composable
private fun BasicsStep(
    name: String,
    type: ActivityType,
    emoji: String,
    onNameChange: (String) -> Unit,
    onTypeChange: (ActivityType) -> Unit,
    onEmojiChange: (String) -> Unit,
    onSelectTemplate: (com.planner.app.feature.create_activity.components.ActivityTemplate) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        TemplatePicker(onSelectTemplate = onSelectTemplate)

        HorizontalDivider()

        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Activity name") },
            placeholder = { Text("e.g. Morning run") },
            modifier = Modifier.fillMaxWidth(),
            shape = ShapeInput,
            singleLine = true,
        )

        // Type selector
        Text("Type", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ActivityType.entries.forEach { t ->
                FilterChip(
                    selected = t == type,
                    onClick = { onTypeChange(t) },
                    label = {
                        Text(
                            t.name.lowercase().replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.bodySmall,
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun ScheduleStep(selectedDays: List<Int>, onToggleDay: (Int) -> Unit) {
    SchedulePicker(selectedDays = selectedDays, onToggleDay = onToggleDay)
}

@Composable
private fun TrackingStep(
    variableTree: List<com.planner.app.domain.model.VariableNode>,
    onTreeChange: (List<com.planner.app.domain.model.VariableNode>) -> Unit,
) {
    Column {
        Text(
            text = "What do you want to track?",
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Leave empty for a simple done/skip activity.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(16.dp))
        VariableTreeEditor(nodes = variableTree, onNodesChanged = onTreeChange)
    }
}

@Composable
private fun GoalStep(
    goal: com.planner.app.domain.model.Goal?,
    onGoalChange: (com.planner.app.domain.model.Goal?) -> Unit,
) {
    Column {
        Text("Set a goal (optional)", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Goals give you a target to work toward and show progress bars in analytics.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
