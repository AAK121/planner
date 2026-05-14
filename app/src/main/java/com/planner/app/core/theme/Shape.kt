package com.planner.app.core.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val PlannerShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),   // chart bars, compliance bars
    small      = RoundedCornerShape(8.dp),   // badges, hints
    medium     = RoundedCornerShape(12.dp),  // inputs, status pills
    large      = RoundedCornerShape(14.dp),  // cards, sections
    extraLarge = RoundedCornerShape(20.dp),  // buttons, pill chips
)

// Additional named shapes used directly in composables
val ShapeContainer = RoundedCornerShape(40.dp)  // large containers
val ShapePill      = RoundedCornerShape(20.dp)  // pill buttons
val ShapeCard      = RoundedCornerShape(14.dp)  // cards
val ShapeInput     = RoundedCornerShape(12.dp)  // text fields
val ShapeBadge     = RoundedCornerShape(8.dp)   // small badges
val ShapeFab       = RoundedCornerShape(16.dp)  // FAB
val ShapeHeatmap   = RoundedCornerShape(2.dp)   // heatmap cells
val ShapeCircle    = CircleShape
