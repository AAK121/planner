package com.planner.app.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.planner.app.data.local.database.dao.ActivityDao
import com.planner.app.data.local.database.dao.ActivityLogDao
import com.planner.app.data.local.database.entity.ActivityEntity
import com.planner.app.data.local.database.entity.ActivityLogEntity

@Database(
    entities = [ActivityEntity::class, ActivityLogEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class PlannerDatabase : RoomDatabase() {
    abstract fun activityDao(): ActivityDao
    abstract fun activityLogDao(): ActivityLogDao

    companion object {
        const val DATABASE_NAME = "planner_db"
    }
}
