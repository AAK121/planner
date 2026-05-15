package com.planner.app.feature.create_activity.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.planner.app.core.theme.ShapeCard
import com.planner.app.core.utils.newId
import com.planner.app.domain.model.VariableNode

@Composable
fun VariableTreeEditor(
    nodes: List<VariableNode>,
    onNodesChange: (List<VariableNode>) -> Unit,
) {
    val items = nodes.filterIsInstance<VariableNode.GroupNode>()

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items.forEach { item ->
            SubActivityCard(
                item = item,
                onLabelChange = { newLabel ->
                    onNodesChange(items.map { if (it.id == item.id) it.copy(label = newLabel) else it })
                },
                onDelete = { onNodesChange(items.filter { it.id != item.id }) },
            )
        }

        OutlinedButton(
            onClick = {
                onNodesChange(items + VariableNode.GroupNode(id = newId(), label = ""))
            },
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text("Add sub-activity", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun SubActivityCard(
    item: VariableNode.GroupNode,
    onLabelChange: (String) -> Unit,
    onDelete: () -> Unit,
) {
    Surface(
        shape = ShapeCard,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = item.label,
                onValueChange = onLabelChange,
                placeholder = {
                    Text("Sub-activity name", style = MaterialTheme.typography.bodySmall)
                },
                modifier = Modifier.weight(1f),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium,
            )
            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Remove",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
