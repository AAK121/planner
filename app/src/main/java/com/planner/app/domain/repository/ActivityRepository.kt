package com.planner.app.domain.repository

import com.planner.app.domain.model.Activity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface ActivityRepository {
    fun observeAll(): Flow<List<Activity>>
    fun observeForToday(today: LocalDate): Flow<List<Activity>>
    suspend fun getAll(): List<Activity>
    suspend fun getById(id: String): Activity?
    suspend fun create(activity: Activity)
    suspend fun update(activity: Activity)
    suspend fun delete(id: String)
    suspend fun archive(id: String)
}
