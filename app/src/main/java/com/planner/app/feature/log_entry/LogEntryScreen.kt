package com.planner.app.feature.log_entry

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.planner.app.core.components.PlannerTopBar
import com.planner.app.core.components.SectionLabel
import com.planner.app.core.components.StatusPill
import com.planner.app.core.theme.ShapeInput
import com.planner.app.core.theme.ShapePill
import com.planner.app.domain.model.LogStatus
import com.planner.app.feature.log_entry.components.RecursiveFormNode

@Composable
fun LogEntryScreen(
    onDone: () -> Unit,
    onBack: () -> Unit,
    viewModel: LogEntryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.savedSuccessfully) {
        if (state.savedSuccessfully) onDone()
    }

    Scaffold(
        topBar = {
            PlannerTopBar(
                title = state.activity?.name ?: "Log entry",
                onBack = onBack,
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            // Status row
            SectionLabel(text = "How did it go?")
            Row(
                modifier = Modifier.padding(horizontal = 28.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                LogStatus.entries.forEach { status ->
                    StatusPill(
                        status = status,
                        selected = state.status == status,
                        onClick = { viewModel.setStatus(status) },
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Duration slider
            SectionLabel(text = "Duration")
            Column(modifier = Modifier.padding(horizontal = 28.dp)) {
                Text(
                    text = "${state.durationMinutes} min",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Slider(
                    value = state.durationMinutes.toFloat(),
                    onValueChange = { viewModel.setDuration(it.toInt()) },
                    valueRange = 5f..180f,
                    steps = 34,
                )
            }

            // Variable tree form
            val tree = state.activity?.variableTree ?: emptyList()
            if (tree.isNotEmpty()) {
                Spacer(Modifier.height(20.dp))
                SectionLabel(text = "Details")
                Column(
                    modifier = Modifier.padding(horizontal = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    tree.forEach { node ->
                        RecursiveFormNode(
                            node = node,
                            data = state.data,
                            onDataChange = viewModel::setDataValue,
                        )
                    }
                }
            }

            // Note
            Spacer(Modifier.height(20.dp))
            SectionLabel(text = "Note (optional)")
            OutlinedTextField(
                value = state.note,
                onValueChange = viewModel::setNote,
                placeholder = { Text("Any thoughts…") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp)
                    .height(100.dp),
                shape = ShapeInput,
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = viewModel::save,
                shape = ShapePill,
                enabled = !state.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp)
                    .height(52.dp),
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Save log", style = MaterialTheme.typography.titleMedium)
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}
