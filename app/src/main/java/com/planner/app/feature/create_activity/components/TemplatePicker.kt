package com.planner.app.feature.create_activity.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.planner.app.core.theme.ShapeCard
import com.planner.app.domain.model.ActivityType
import com.planner.app.domain.model.VariableNode
import com.planner.app.domain.model.ValueType
import com.planner.app.core.utils.newId

data class ActivityTemplate(
    val emoji: String,
    val name: String,
    val color: String,
    val type: ActivityType,
    val defaultDays: List<Int>,
    val tree: List<VariableNode>,
)

val activityTemplates = listOf(
    ActivityTemplate(
        emoji = "🏋️", name = "Workout", color = "#5B9E9E",
        type = ActivityType.RECURRING, defaultDays = listOf(1, 3, 5),
        tree = listOf(
            VariableNode.ListNode(
                id = newId(), label = "Exercises",
                itemTemplate = listOf(
                    VariableNode.ValueNode(id = newId(), label = "Exercise name", valueType = ValueType.TEXT),
                    VariableNode.ListNode(id = newId(), label = "Sets", itemTemplate = listOf(
                        VariableNode.ValueNode(id = newId(), label = "Weight (kg)", valueType = ValueType.NUMBER),
                        VariableNode.ValueNode(id = newId(), label = "Reps", valueType = ValueType.NUMBER),
                    ))
                )
            )
        ),
    ),
    ActivityTemplate(
        emoji = "🏃", name = "Cardio", color = "#C9A24E",
        type = ActivityType.RECURRING, defaultDays = listOf(2, 4, 6),
        tree = listOf(
            VariableNode.ValueNode(id = newId(), label = "Distance (km)", valueType = ValueType.NUMBER),
            VariableNode.ValueNode(id = newId(), label = "Pace (min/km)", valueType = ValueType.DURATION),
        ),
    ),
    ActivityTemplate(
        emoji = "📖", name = "Reading", color = "#D4816B",
        type = ActivityType.RECURRING, defaultDays = listOf(1, 2, 3, 4, 5, 6, 7),
        tree = listOf(
            VariableNode.ValueNode(id = newId(), label = "Book title", valueType = ValueType.TEXT),
            VariableNode.ValueNode(id = newId(), label = "Pages read", valueType = ValueType.NUMBER),
            VariableNode.ValueNode(id = newId(), label = "Takeaway", valueType = ValueType.TEXT),
        ),
    ),
    ActivityTemplate(
        emoji = "🧘", name = "Meditation", color = "#7A9E7A",
        type = ActivityType.RECURRING, defaultDays = listOf(1, 2, 3, 4, 5, 6, 7),
        tree = listOf(
            VariableNode.ValueNode(id = newId(), label = "Technique", valueType = ValueType.TEXT),
        ),
    ),
    ActivityTemplate(
        emoji = "📝", name = "Journaling", color = "#D4816B",
        type = ActivityType.RECURRING, defaultDays = listOf(1, 2, 3, 4, 5, 6, 7),
        tree = listOf(
            VariableNode.ValueNode(id = newId(), label = "Entry", valueType = ValueType.TEXT),
        ),
    ),
)

@Composable
fun TemplatePicker(
    onSelectTemplate: (ActivityTemplate) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = "Start from a template",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(activityTemplates) { template ->
                Surface(
                    shape = ShapeCard,
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp,
                    modifier = Modifier.clickable { onSelectTemplate(template) },
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(template.emoji, style = MaterialTheme.typography.headlineMedium)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = template.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                }
            }
        }
    }
}
