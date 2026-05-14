package com.planner.app.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.planner.app.data.local.database.entity.ActivityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(activity: ActivityEntity)

    @Update
    suspend fun update(activity: ActivityEntity)

    @Delete
    suspend fun delete(activity: ActivityEntity)

    @Query("SELECT * FROM activities WHERE isArchived = 0 ORDER BY createdAt ASC")
    fun observeAll(): Flow<List<ActivityEntity>>

    @Query("SELECT * FROM activities WHERE id = :id")
    suspend fun getById(id: String): ActivityEntity?

    @Query("SELECT * FROM activities WHERE type = :type AND isArchived = 0")
    fun observeByType(type: String): Flow<List<ActivityEntity>>

    @Query("UPDATE activities SET isArchived = 1 WHERE id = :id")
    suspend fun archive(id: String)

    @Query("SELECT * FROM activities WHERE isArchived = 0")
    suspend fun getAll(): List<ActivityEntity>
}
