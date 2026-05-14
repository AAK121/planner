package com.planner.app.data.repository

import com.planner.app.data.local.database.dao.ActivityDao
import com.planner.app.data.local.database.entity.ActivityEntity
import com.planner.app.domain.model.Activity
import com.planner.app.domain.model.ActivityType
import com.planner.app.domain.model.Goal
import com.planner.app.domain.model.Schedule
import com.planner.app.domain.model.VariableNode
import com.planner.app.domain.repository.ActivityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDate
import javax.inject.Inject

class ActivityRepositoryImpl @Inject constructor(
    private val dao: ActivityDao,
) : ActivityRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override fun observeAll(): Flow<List<Activity>> =
        dao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override fun observeForToday(today: LocalDate): Flow<List<Activity>> =
        observeAll().map { all ->
            all.filter { it.schedule.daysOfWeek.contains(today.dayOfWeek.value) }
        }

    override suspend fun getById(id: String): Activity? =
        dao.getById(id)?.toDomain()

    override suspend fun create(activity: Activity) =
        dao.insert(activity.toEntity())

    override suspend fun update(activity: Activity) =
        dao.update(activity.toEntity())

    override suspend fun delete(id: String) {
        dao.getById(id)?.let { dao.delete(it) }
    }

    override suspend fun archive(id: String) = dao.archive(id)

    private fun ActivityEntity.toDomain(): Activity = Activity(
        id         = id,
        name       = name,
        type       = ActivityType.valueOf(type),
        color      = color,
        emoji      = emoji,
        schedule   = json.decodeFromString(scheduleJson),
        variableTree = if (treeJson == "[]") emptyList()
                       else json.decodeFromString<List<VariableNode>>(treeJson),
        goal       = if (goalJson == "null") null else json.decodeFromString<Goal>(goalJson),
        createdAt  = createdAt,
        isArchived = isArchived,
    )

    private fun Activity.toEntity(): ActivityEntity = ActivityEntity(
        id           = id,
        name         = name,
        type         = type.name,
        color        = color,
        emoji        = emoji,
        scheduleJson = json.encodeToString(schedule),
        treeJson     = json.encodeToString(variableTree),
        goalJson     = if (goal == null) "null" else json.encodeToString(goal),
        createdAt    = createdAt,
        isArchived   = isArchived,
    )
}
