package com.planner.app.feature.log_entry.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.planner.app.domain.model.VariableNode

@Composable
fun RecursiveFormNode(
    node: VariableNode,
    data: Map<String, String>,
    onDataChange: (String, String) -> Unit,
    depth: Int = 0,            // unused, kept for call-site compatibility
    modifier: Modifier = Modifier,
) {
    if (node !is VariableNode.GroupNode) return

    Column(modifier = modifier.fillMaxWidth()) {
        if (node.label.isNotBlank()) {
            Text(
                text = node.label,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}
