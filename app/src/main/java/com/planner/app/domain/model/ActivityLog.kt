package com.planner.app.domain.model

import java.time.LocalDate

data class ActivityLog(
    val id: String,
    val activityId: String,
    val date: LocalDate,
    val status: LogStatus,
    val durationMinutes: Int,
    val data: Map<String, String> = emptyMap(), // nodeId → value as string
    val note: String = "",
    val createdAt: Long,
)
