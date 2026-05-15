package com.planner.app.di

import android.content.Context
import androidx.room.Room
import com.planner.app.data.local.database.PlannerDatabase
import com.planner.app.data.local.database.dao.ActivityDao
import com.planner.app.data.local.database.dao.ActivityLogDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun providePlannerDatabase(@ApplicationContext context: Context): PlannerDatabase =
        Room.databaseBuilder(context, PlannerDatabase::class.java, PlannerDatabase.DATABASE_NAME)
            .addMigrations(PlannerDatabase.MIGRATION_1_2, PlannerDatabase.MIGRATION_2_3)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideActivityDao(db: PlannerDatabase): ActivityDao = db.activityDao()

    @Provides
    fun provideActivityLogDao(db: PlannerDatabase): ActivityLogDao = db.activityLogDao()
}
