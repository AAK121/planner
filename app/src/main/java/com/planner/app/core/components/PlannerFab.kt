package com.planner.app.core.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.planner.app.core.theme.ShapeFab

@Composable
fun PlannerFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Rounded.Add,
    containerColor: Color = MaterialTheme.colorScheme.primary,
) {
    FloatingActionButton(
        onClick = onClick,
        shape = ShapeFab,
        containerColor = containerColor,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
        modifier = modifier.size(52.dp),
    ) {
        Icon(imageVector = icon, contentDescription = "Add", modifier = Modifier.size(22.dp))
    }
}
