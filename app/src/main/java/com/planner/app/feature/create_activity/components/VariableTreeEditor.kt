package com.planner.app.feature.create_activity.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.FlowRow
import com.planner.app.core.theme.ShapeCard
import com.planner.app.core.theme.ShapeInput
import com.planner.app.core.utils.newId
import com.planner.app.domain.model.ValueType
import com.planner.app.domain.model.VariableNode

@Composable
fun VariableTreeEditor(
    nodes: List<VariableNode>,
    onNodesChanged: (List<VariableNode>) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        nodes.forEachIndexed { index, node ->
            NodeCard(
                node = node,
                depth = 0,
                onUpdate = { updated ->
                    onNodesChanged(nodes.toMutableList().also { it[index] = updated })
                },
                onRemove = {
                    onNodesChanged(nodes.toMutableList().also { it.removeAt(index) })
                },
            )
            Spacer(Modifier.height(8.dp))
        }

        AddNodeRow(
            onAddGroup = {
                onNodesChanged(nodes + VariableNode.GroupNode(id = newId(), label = "Group"))
            },
            onAddList = {
                onNodesChanged(nodes + VariableNode.ListNode(id = newId(), label = "List"))
            },
            onAddValue = {
                onNodesChanged(nodes + VariableNode.ValueNode(id = newId(), label = "Value", valueType = ValueType.TEXT))
            },
        )
    }
}

@Composable
private fun NodeCard(
    node: VariableNode,
    depth: Int,
    onUpdate: (VariableNode) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = ShapeCard,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = (1 + depth).dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(start = (depth * 16).dp),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val typeLabel = when (node) {
                    is VariableNode.GroupNode -> "Group"
                    is VariableNode.ListNode  -> "List"
                    is VariableNode.ValueNode -> node.valueType.name.lowercase().replaceFirstChar { it.uppercase() }
                }
                Text(
                    text = typeLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onRemove, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Rounded.Close, contentDescription = "Remove", modifier = Modifier.size(16.dp))
                }
            }

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = node.label,
                onValueChange = { newLabel ->
                    val updated: VariableNode = when (node) {
                        is VariableNode.GroupNode -> node.copy(label = newLabel)
                        is VariableNode.ListNode  -> node.copy(label = newLabel)
                        is VariableNode.ValueNode -> node.copy(label = newLabel)
                    }
                    onUpdate(updated)
                },
                label = { Text("Label") },
                modifier = Modifier.fillMaxWidth(),
                shape = ShapeInput,
                singleLine = true,
            )

            // Children for Group / List
            when (node) {
                is VariableNode.GroupNode -> {
                    Spacer(Modifier.height(12.dp))
                    GroupDayPicker(
                        selectedDays = node.daysOfWeek,
                        onDaysChange = { onUpdate(node.copy(daysOfWeek = it)) },
                    )
                    Spacer(Modifier.height(8.dp))
                    VariableTreeEditor(
                        nodes = node.children,
                        onNodesChanged = { onUpdate(node.copy(children = it)) },
                    )
                }
                is VariableNode.ListNode -> {
                    Spacer(Modifier.height(12.dp))
                    VariableTreeEditor(
                        nodes = node.itemTemplate,
                        onNodesChanged = { onUpdate(node.copy(itemTemplate = it)) },
                    )
                }
                is VariableNode.ValueNode -> {
                    Spacer(Modifier.height(8.dp))
                    ValueTypeSelector(
                        selected = node.valueType,
                        onSelect = { onUpdate(node.copy(valueType = it)) },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ValueTypeSelector(selected: ValueType, onSelect: (ValueType) -> Unit) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        ValueType.entries.forEach { type ->
            val isSelected = type == selected
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(type) },
                label = { Text(type.name.lowercase(), style = MaterialTheme.typography.labelMedium) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    selectedBorderColor = MaterialTheme.colorScheme.primary,
                    selectedBorderWidth = 1.5.dp,
                ),
            )
        }
    }
}

@Composable
private fun GroupDayPicker(
    selectedDays: List<Int>,
    onDaysChange: (List<Int>) -> Unit,
) {
    val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")
    Column {
        Text(
            text = if (selectedDays.isEmpty()) "Active every day" else "Active on selected days",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            dayLabels.forEachIndexed { index, label ->
                val dayNum = index + 1
                val isSelected = dayNum in selectedDays
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        val updated = if (isSelected) selectedDays - dayNum else (selectedDays + dayNum).sorted()
                        onDaysChange(updated)
                    },
                    label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        selectedBorderColor = MaterialTheme.colorScheme.primary,
                        selectedBorderWidth = 1.5.dp,
                    ),
                )
            }
        }
    }
}

@Composable
private fun AddNodeRow(
    onAddGroup: () -> Unit,
    onAddList: () -> Unit,
    onAddValue: () -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("Group" to onAddGroup, "List" to onAddList, "Value" to onAddValue).forEach { (label, action) ->
            OutlinedButton(
                onClick = action,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text(label, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
