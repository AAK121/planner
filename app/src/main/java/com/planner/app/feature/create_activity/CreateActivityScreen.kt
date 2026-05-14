package com.planner.app.feature.create_activity

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.planner.app.core.components.PlannerTopBar
import com.planner.app.core.theme.ShapeInput
import com.planner.app.core.theme.ShapePill
import com.planner.app.domain.model.ActivityType
import com.planner.app.domain.model.Goal
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
            StepIndicator(
                currentStep = state.currentStep,
                steps = steps,
                onStepClick = viewModel::goToStep,
            )

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
private fun StepIndicator(currentStep: Int, steps: List<String>, onStepClick: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        steps.forEachIndexed { index, label ->
            FilterChip(
                selected = index == currentStep || index < currentStep,
                onClick = { onStepClick(index) },
                label = { Text(label, style = MaterialTheme.typography.labelMedium) },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GoalStep(
    goal: Goal?,
    onGoalChange: (Goal?) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Set a goal (optional)", style = MaterialTheme.typography.titleMedium)
        Text(
            text = "Goals give you a target to work toward and show progress in analytics.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (goal == null) {
            OutlinedButton(
                onClick = { onGoalChange(Goal(targetValue = 1.0, unit = "", period = "daily")) },
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Add goal", style = MaterialTheme.typography.bodySmall)
            }
        } else {
            var targetText by remember(goal.targetValue) {
                mutableStateOf(
                    if (goal.targetValue == goal.targetValue.toLong().toDouble())
                        goal.targetValue.toLong().toString()
                    else goal.targetValue.toString()
                )
            }

            OutlinedTextField(
                value = targetText,
                onValueChange = { v ->
                    val filtered = v.filter { it.isDigit() || it == '.' }
                    targetText = filtered
                    filtered.toDoubleOrNull()?.let { onGoalChange(goal.copy(targetValue = it)) }
                },
                label = { Text("Target value") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                shape = ShapeInput,
                singleLine = true,
            )

            OutlinedTextField(
                value = goal.unit,
                onValueChange = { onGoalChange(goal.copy(unit = it)) },
                label = { Text("Unit") },
                placeholder = { Text("e.g. minutes, reps, km") },
                modifier = Modifier.fillMaxWidth(),
                shape = ShapeInput,
                singleLine = true,
            )

            Text("Period", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("daily", "weekly", "monthly").forEach { period ->
                    FilterChip(
                        selected = goal.period == period,
                        onClick = { onGoalChange(goal.copy(period = period)) },
                        label = {
                            Text(
                                period.replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.labelMedium,
                            )
                        },
                    )
                }
            }

            TextButton(onClick = { onGoalChange(null) }) {
                Text("Remove goal", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
