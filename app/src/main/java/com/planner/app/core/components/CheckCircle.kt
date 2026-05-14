package com.planner.app.core.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.planner.app.core.theme.ShapeCircle

@Composable
fun CheckCircle(
    checked: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 26.dp,
    activeColor: Color = MaterialTheme.colorScheme.primary,
) {
    val borderColor = MaterialTheme.colorScheme.outline

    AnimatedContent(
        targetState = checked,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "CheckCircle",
    ) { isChecked ->
        Box(
            modifier = modifier
                .size(size)
                .clip(ShapeCircle)
                .then(
                    if (isChecked) Modifier.drawBehind { drawCircle(activeColor) }
                    else Modifier.border(1.5.dp, borderColor, ShapeCircle)
                )
                .clickable { onToggle() },
            contentAlignment = Alignment.Center,
        ) {
            if (isChecked) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = "Done",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp),
                )
            }
        }
    }
}
