package com.planner.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Schedule(
    val daysOfWeek: List<Int> = emptyList(),    // 1=Mon … 7=Sun
    val timesOfDay: List<String> = emptyList(), // "HH:mm"
    val hasReminder: Boolean = false,
    val reminderMinutesBefore: Int = 30,
    val targetFrequencyPerWeek: Int? = null,    // for OCCASIONAL
    val dueDate: Long? = null,                  // epoch millis for ONE_OFF
)

@Serializable
data class Goal(
    val targetValue: Double,
    val unit: String,
    val period: String,  // "daily" | "weekly" | "monthly"
)

data class Activity(
    val id: String,
    val name: String,
    val type: ActivityType,
    val color: String,
    val emoji: String,
    val schedule: Schedule,
    val variableTree: List<VariableNode> = emptyList(),
    val goal: Goal? = null,
    val createdAt: Long,
    val isArchived: Boolean = false,
)
