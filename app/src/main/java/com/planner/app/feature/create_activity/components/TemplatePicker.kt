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
            VariableNode.GroupNode(id = newId(), label = "Upper Body"),
            VariableNode.GroupNode(id = newId(), label = "Lower Body"),
            VariableNode.GroupNode(id = newId(), label = "Cardio"),
        ),
    ),
    ActivityTemplate(
        emoji = "📖", name = "Reading", color = "#D4816B",
        type = ActivityType.RECURRING, defaultDays = listOf(1, 2, 3, 4, 5, 6, 7),
        tree = emptyList(),
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
