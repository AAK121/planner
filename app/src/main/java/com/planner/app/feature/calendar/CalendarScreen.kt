package com.planner.app.feature.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
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
import com.planner.app.core.navigation.Screen
import com.planner.app.core.theme.ShapeCircle
import com.planner.app.domain.model.LogStatus
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private val DATE_HEADER_FMT = DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.getDefault())

@Composable
fun CalendarScreen(
    onBack: () -> Unit,
    onLogActivity: (String) -> Unit,
    onNavigateToTab: (Screen) -> Unit,
    viewModel: CalendarViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { PlannerTopBar(title = "Calendar", onBack = onBack) },
        bottomBar = {
            PlannerBottomBar(
                currentRoute = Screen.Calendar.route,
                onNavigate = onNavigateToTab,
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        CalendarLayout(
            state = state,
            innerPadding = innerPadding,
            onPrevMonth = viewModel::prevMonth,
            onNextMonth = viewModel::nextMonth,
            onSelectDate = viewModel::selectDate,
            onLogActivity = onLogActivity,
        )
    }
}

@Composable
fun CalendarContent(
    innerPadding: PaddingValues,
    onLogActivity: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: CalendarViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    CalendarLayout(
        state = state,
        innerPadding = innerPadding,
        onPrevMonth = viewModel::prevMonth,
        onNextMonth = viewModel::nextMonth,
        onSelectDate = viewModel::selectDate,
        onLogActivity = onLogActivity,
        onNavigateToSettings = onNavigateToSettings,
    )
}

@Composable
private fun CalendarLayout(
    state: CalendarUiState,
    innerPadding: PaddingValues,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSelectDate: (LocalDate) -> Unit,
    onLogActivity: (String) -> Unit,
    onNavigateToSettings: (() -> Unit)? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
    ) {
        if (onNavigateToSettings != null) {
            PlannerTopBar(
                title = "Calendar",
                windowInsets = WindowInsets(0),
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                },
            )
        }

        // Fixed calendar block
        Column(modifier = Modifier.padding(horizontal = 28.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                IconButton(onClick = onPrevMonth) {
                    Icon(Icons.Rounded.ChevronLeft, contentDescription = "Previous")
                }
                Text(
                    text = state.currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault()) +
                            " ${state.currentMonth.year}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onNextMonth) {
                    Icon(Icons.Rounded.ChevronRight, contentDescription = "Next")
                }
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("M", "T", "W", "T", "F", "S", "S").forEach { d ->
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

            val firstDay = state.currentMonth.atDay(1)
            val offset = firstDay.dayOfWeek.value - 1
            val totalDays = state.currentMonth.lengthOfMonth()

            LazyVerticalGrid(columns = GridCells.Fixed(7), userScrollEnabled = false) {
                items(offset) { Box(Modifier.size(44.dp)) }
                items(totalDays) { dayIndex ->
                    val date = state.currentMonth.atDay(dayIndex + 1)
                    val isSelected = date == state.selectedDate
                    val isToday = date == LocalDate.now()
                    val isDone = state.logsByDate[date]?.any { it.status == LogStatus.DONE } == true
                    val hasActivity = state.scheduledByDate.containsKey(date) || state.logsByDate.containsKey(date)

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
                            .clickable { onSelectDate(date) },
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
                            if (hasActivity) {
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .clip(ShapeCircle)
                                        .background(
                                            when {
                                                isSelected -> MaterialTheme.colorScheme.onPrimary
                                                isDone     -> MaterialTheme.colorScheme.primary
                                                else       -> MaterialTheme.colorScheme.onSurfaceVariant
                                            }
                                        ),
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }

        HorizontalDivider()

        // Persistent activities section — always visible, defaults to today
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp)
                .padding(top = 16.dp, bottom = 16.dp),
        ) {
            Text(
                text = state.selectedDate.format(DATE_HEADER_FMT),
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.height(12.dp))

            if (state.activitiesForSelected.isEmpty()) {
                Text(
                    text = "No activities scheduled for this day.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            } else {
                val logsByActivity = remember(state.logsForSelected) {
                    state.logsForSelected.associateBy { it.activityId }
                }
                state.activitiesForSelected.forEach { activity ->
                    val log = logsByActivity[activity.id]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onLogActivity(activity.id) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(activity.emoji, style = MaterialTheme.typography.bodyLarge)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = activity.name,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f),
                        )
                        log?.status?.let { status ->
                            Text(
                                text = status.name,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}
