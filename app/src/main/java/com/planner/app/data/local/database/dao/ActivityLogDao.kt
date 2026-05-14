package com.planner.app.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.planner.app.data.local.database.entity.ActivityLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: ActivityLogEntity)

    @Update
    suspend fun update(log: ActivityLogEntity)

    @Query("DELETE FROM activity_logs WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM activity_logs WHERE activityId = :activityId ORDER BY dateEpochDay DESC")
    fun observeForActivity(activityId: String): Flow<List<ActivityLogEntity>>

    @Query("SELECT * FROM activity_logs WHERE activityId = :activityId ORDER BY dateEpochDay DESC")
    suspend fun getForActivity(activityId: String): List<ActivityLogEntity>

    @Query("SELECT * FROM activity_logs WHERE dateEpochDay = :epochDay")
    fun observeForDay(epochDay: Long): Flow<List<ActivityLogEntity>>

    @Query("SELECT * FROM activity_logs WHERE activityId = :activityId AND dateEpochDay = :epochDay LIMIT 1")
    suspend fun getForActivityAndDay(activityId: String, epochDay: Long): ActivityLogEntity?

    @Query("""
        SELECT * FROM activity_logs
        WHERE activityId = :activityId AND dateEpochDay BETWEEN :fromDay AND :toDay
        ORDER BY dateEpochDay ASC
    """)
    suspend fun getForActivityInRange(activityId: String, fromDay: Long, toDay: Long): List<ActivityLogEntity>

    @Query("SELECT * FROM activity_logs WHERE dateEpochDay BETWEEN :fromDay AND :toDay ORDER BY dateEpochDay ASC")
    suspend fun getAllInRange(fromDay: Long, toDay: Long): List<ActivityLogEntity>

    @Query("SELECT * FROM activity_logs WHERE activityId = :activityId ORDER BY dateEpochDay DESC LIMIT :limit")
    suspend fun getRecentForActivity(activityId: String, limit: Int): List<ActivityLogEntity>
}
