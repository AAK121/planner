package com.planner.app.domain.repository

import com.planner.app.domain.model.ActivityLog
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface LogRepository {
    fun observeForActivity(activityId: String): Flow<List<ActivityLog>>
    fun observeForDay(date: LocalDate): Flow<List<ActivityLog>>
    fun observeAllInRange(from: LocalDate, to: LocalDate): Flow<List<ActivityLog>>
    suspend fun getForActivity(activityId: String): List<ActivityLog>
    suspend fun getForActivityInRange(activityId: String, from: LocalDate, to: LocalDate): List<ActivityLog>
    suspend fun getAllInRange(from: LocalDate, to: LocalDate): List<ActivityLog>
    suspend fun getForActivityAndDay(activityId: String, date: LocalDate): ActivityLog?
    suspend fun save(log: ActivityLog)
    suspend fun delete(logId: String)
}
