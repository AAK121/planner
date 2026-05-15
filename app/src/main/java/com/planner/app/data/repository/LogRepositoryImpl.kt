package com.planner.app.data.repository

import com.planner.app.data.local.database.dao.ActivityLogDao
import com.planner.app.data.local.database.entity.ActivityLogEntity
import com.planner.app.domain.model.ActivityLog
import com.planner.app.domain.model.LogStatus
import com.planner.app.domain.repository.LogRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDate
import javax.inject.Inject

class LogRepositoryImpl @Inject constructor(
    private val dao: ActivityLogDao,
) : LogRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override fun observeForActivity(activityId: String): Flow<List<ActivityLog>> =
        dao.observeForActivity(activityId)
            .map { it.map { e -> e.toDomain() } }
            .flowOn(Dispatchers.Default)
            .distinctUntilChanged()

    override fun observeForDay(date: LocalDate): Flow<List<ActivityLog>> =
        dao.observeForDay(date.toEpochDay())
            .map { it.map { e -> e.toDomain() } }
            .flowOn(Dispatchers.Default)
            .distinctUntilChanged()

    override fun observeAllInRange(from: LocalDate, to: LocalDate): Flow<List<ActivityLog>> =
        dao.observeAllInRange(from.toEpochDay(), to.toEpochDay())
            .map { it.map { e -> e.toDomain() } }
            .flowOn(Dispatchers.Default)
            .distinctUntilChanged()

    override suspend fun getForActivity(activityId: String): List<ActivityLog> =
        dao.getForActivity(activityId).map { it.toDomain() }

    override suspend fun getForActivityInRange(activityId: String, from: LocalDate, to: LocalDate): List<ActivityLog> =
        dao.getForActivityInRange(activityId, from.toEpochDay(), to.toEpochDay()).map { it.toDomain() }

    override suspend fun getAllInRange(from: LocalDate, to: LocalDate): List<ActivityLog> =
        dao.getAllInRange(from.toEpochDay(), to.toEpochDay()).map { it.toDomain() }

    override suspend fun getForActivityAndDay(activityId: String, date: LocalDate): ActivityLog? =
        dao.getForActivityAndDay(activityId, date.toEpochDay())?.toDomain()

    override suspend fun save(log: ActivityLog) = dao.insert(log.toEntity())

    override suspend fun delete(logId: String) = dao.deleteById(logId)

    private fun ActivityLogEntity.toDomain(): ActivityLog = ActivityLog(
        id              = id,
        activityId      = activityId,
        date            = LocalDate.ofEpochDay(dateEpochDay),
        status          = LogStatus.valueOf(status),
        durationMinutes = durationMinutes,
        data            = json.decodeFromString(dataJson),
        note            = note,
        createdAt       = createdAt,
    )

    private fun ActivityLog.toEntity(): ActivityLogEntity = ActivityLogEntity(
        id              = id,
        activityId      = activityId,
        dateEpochDay    = date.toEpochDay(),
        status          = status.name,
        durationMinutes = durationMinutes,
        dataJson        = json.encodeToString(data),
        note            = note,
        createdAt       = createdAt,
    )
}
