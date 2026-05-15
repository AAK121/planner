package com.planner.app.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.planner.app.data.local.database.dao.ActivityDao
import com.planner.app.data.local.database.dao.ActivityLogDao
import com.planner.app.data.local.database.entity.ActivityEntity
import com.planner.app.data.local.database.entity.ActivityLogEntity

@Database(
    entities = [ActivityEntity::class, ActivityLogEntity::class],
    version = 3,
    exportSchema = false,
)
abstract class PlannerDatabase : RoomDatabase() {
    abstract fun activityDao(): ActivityDao
    abstract fun activityLogDao(): ActivityLogDao

    companion object {
        const val DATABASE_NAME = "planner_db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE activities ADD COLUMN trackAnalytics INTEGER NOT NULL DEFAULT 1")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE activities ADD COLUMN className TEXT DEFAULT NULL")
            }
        }
    }
}
