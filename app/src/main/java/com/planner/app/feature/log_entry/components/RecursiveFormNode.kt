package com.planner.app.feature.log_entry.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.planner.app.core.theme.ShapeCard
import com.planner.app.core.theme.ShapeInput
import com.planner.app.domain.model.ValueType
import com.planner.app.domain.model.VariableNode
import java.time.LocalDate

@Composable
fun RecursiveFormNode(
    node: VariableNode,
    data: Map<String, String>,
    onDataChange: (String, String) -> Unit,
    depth: Int = 0,
    modifier: Modifier = Modifier,
) {
    // Skip GroupNodes that are not scheduled for today
    if (node is VariableNode.GroupNode && node.daysOfWeek.isNotEmpty()) {
        val todayDow = LocalDate.now().dayOfWeek.value // 1=Mon…7=Sun
        if (todayDow !in node.daysOfWeek) return
    }

    Surface(
        shape = ShapeCard,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = (1 + depth).dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(start = (depth * 12).dp),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = node.label,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(10.dp))

            when (node) {
                is VariableNode.GroupNode -> {
                    node.children.forEach { child ->
                        RecursiveFormNode(
                            node = child,
                            data = data,
                            onDataChange = onDataChange,
                            depth = depth + 1,
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }
                is VariableNode.ListNode -> {
                    val countKey = "${node.id}__count"
                    val count = (data[countKey]?.toIntOrNull() ?: 1).coerceAtLeast(1)

                    repeat(count) { itemIndex ->
                        Text(
                            text = "${node.label} ${itemIndex + 1}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(4.dp))
                        node.itemTemplate.forEach { child ->
                            val prefixedChild = when (child) {
                                is VariableNode.ValueNode -> child.copy(id = "${node.id}__${itemIndex}__${child.id}")
                                is VariableNode.GroupNode -> child.copy(id = "${node.id}__${itemIndex}__${child.id}")
                                is VariableNode.ListNode  -> child.copy(id = "${node.id}__${itemIndex}__${child.id}")
                            }
                            RecursiveFormNode(
                                node = prefixedChild,
                                data = data,
                                onDataChange = onDataChange,
                                depth = depth + 1,
                            )
                            Spacer(Modifier.height(6.dp))
                        }
                        Spacer(Modifier.height(8.dp))
                    }

                    TextButton(
                        onClick = { onDataChange(countKey, (count + 1).toString()) },
                        contentPadding = PaddingValues(0.dp),
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Add ${node.label}", style = MaterialTheme.typography.bodySmall)
                    }
                }
                is VariableNode.ValueNode -> {
                    ValueInput(
                        node = node,
                        value = data[node.id] ?: "",
                        onValueChange = { onDataChange(node.id, it) },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ValueInput(
    node: VariableNode.ValueNode,
    value: String,
    onValueChange: (String) -> Unit,
) {
    when (node.valueType) {
        ValueType.BOOLEAN -> {
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
            ) {
                OutlinedTextField(
                    value = if (value == "true") "True" else if (value == "false") "False" else "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(node.label) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = ShapeInput,
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                ) {
                    DropdownMenuItem(
                        text = { Text("True") },
                        onClick = { onValueChange("true"); expanded = false },
                    )
                    DropdownMenuItem(
                        text = { Text("False") },
                        onClick = { onValueChange("false"); expanded = false },
                    )
                }
            }
        }
        ValueType.CHOICE -> {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                node.choices.forEach { choice ->
                    FilterChip(
                        selected = value == choice,
                        onClick = { onValueChange(choice) },
                        label = { Text(choice, style = MaterialTheme.typography.bodySmall) },
                    )
                }
            }
        }
        ValueType.NUMBER -> {
            OutlinedTextField(
                value = value,
                onValueChange = { v -> onValueChange(v.filter { it.isDigit() || it == '.' }) },
                label = { Text(node.label) },
                modifier = Modifier.fillMaxWidth(),
                shape = ShapeInput,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                suffix = if (node.unit.isNotEmpty()) {
                    { Text(node.unit, style = MaterialTheme.typography.bodySmall) }
                } else null,
            )
        }
        ValueType.DURATION -> {
            OutlinedTextField(
                value = value,
                onValueChange = { v -> onValueChange(v.filter { it.isDigit() }) },
                label = { Text(node.label) },
                modifier = Modifier.fillMaxWidth(),
                shape = ShapeInput,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                suffix = { Text("min", style = MaterialTheme.typography.bodySmall) },
            )
        }
        ValueType.TEXT -> {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = { Text(node.label) },
                modifier = Modifier.fillMaxWidth(),
                shape = ShapeInput,
                singleLine = false,
                suffix = if (node.unit.isNotEmpty()) {
                    { Text(node.unit, style = MaterialTheme.typography.bodySmall) }
                } else null,
            )
        }
    }
}
