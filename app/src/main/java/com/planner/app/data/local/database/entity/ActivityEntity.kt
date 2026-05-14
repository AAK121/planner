package com.planner.app.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activities")
data class ActivityEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: String,           // RECURRING | OCCASIONAL | ONE_OFF
    val color: String,          // hex color for colorful theme
    val emoji: String,
    val scheduleJson: String,   // serialized Schedule
    val treeJson: String,       // serialized VariableNode tree (nullable → "null")
    val goalJson: String,       // serialized Goal (nullable → "null")
    val createdAt: Long,
    val isArchived: Boolean = false,
)
