package com.planner.app.data.local.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "activity_logs",
    foreignKeys = [
        ForeignKey(
            entity = ActivityEntity::class,
            parentColumns = ["id"],
            childColumns = ["activityId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("activityId"), Index("dateEpochDay")],
)
data class ActivityLogEntity(
    @PrimaryKey val id: String,
    val activityId: String,
    val dateEpochDay: Long,     // LocalDate.toEpochDay()
    val status: String,         // DONE | PARTIAL | SKIPPED
    val durationMinutes: Int,
    val dataJson: String,       // serialized Map<nodeId, Any>
    val note: String = "",
    val createdAt: Long,
)
