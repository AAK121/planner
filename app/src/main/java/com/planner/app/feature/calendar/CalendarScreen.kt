package com.planner.app.feature.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.planner.app.core.components.PlannerBottomBar
import com.planner.app.core.components.PlannerTopBar
import com.planner.app.core.theme.ShapeCircle
import com.planner.app.domain.model.LogStatus
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onBack: () -> Unit,
    onLogActivity: (String) -> Unit,
    viewModel: CalendarViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    var showSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { PlannerTopBar(title = "Calendar", onBack = onBack) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 28.dp),
        ) {
            // Month navigation
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                IconButton(onClick = viewModel::prevMonth) {
                    Icon(Icons.Rounded.ChevronLeft, contentDescription = "Previous")
                }
                Text(
                    text = state.currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault()) +
                            " ${state.currentMonth.year}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = viewModel::nextMonth) {
                    Icon(Icons.Rounded.ChevronRight, contentDescription = "Next")
                }
            }

            // Day labels
            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("M","T","W","T","F","S","S").forEach { d ->
                    Text(
                        text = d,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Month grid
            val firstDay = state.currentMonth.atDay(1)
            val offset = (firstDay.dayOfWeek.value - 1) // Mon=0
            val totalDays = state.currentMonth.lengthOfMonth()

            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                userScrollEnabled = false,
            ) {
                items(offset) { Box(Modifier.size(44.dp)) }

                items(totalDays) { dayIndex ->
                    val date = state.currentMonth.atDay(dayIndex + 1)
                    val hasLogs = state.logsByDate.containsKey(date)
                    val isSelected = date == state.selectedDate
                    val isToday = date == LocalDate.now()
                    val isDone = state.logsByDate[date]?.any { it.status == LogStatus.DONE } == true

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(ShapeCircle)
                            .background(
                                when {
                                    isSelected -> MaterialTheme.colorScheme.primary
                                    isToday    -> MaterialTheme.colorScheme.surfaceVariant
                                    else       -> Color.Transparent
                                }
                            )
                            .clickable {
                                viewModel.selectDate(date)
                                showSheet = true
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${dayIndex + 1}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = when {
                                    isSelected -> MaterialTheme.colorScheme.onPrimary
                                    isToday    -> MaterialTheme.colorScheme.primary
                                    else       -> MaterialTheme.colorScheme.onBackground
                                },
                            )
                            if (hasLogs) {
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .clip(ShapeCircle)
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.onPrimary
                                            else if (isDone) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }

        if (showSheet) {
            ModalBottomSheet(onDismissRequest = { showSheet = false }) {
                Column(modifier = Modifier.padding(horizontal = 28.dp, vertical = 16.dp)) {
                    Text(
                        text = state.selectedDate.toString(),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(Modifier.height(16.dp))
                    if (state.activitiesForSelected.isEmpty()) {
                        Text("No logs for this day.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        state.activitiesForSelected.forEach { activity ->
                            val log = state.logsForSelected.find { it.activityId == activity.id }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onLogActivity(activity.id) }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(activity.emoji, style = MaterialTheme.typography.bodyLarge)
                                Spacer(Modifier.width(12.dp))
                                Text(activity.name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                                log?.status?.let { status ->
                                    Text(status.name, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}
