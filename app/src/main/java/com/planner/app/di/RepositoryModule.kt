package com.planner.app.di

import com.planner.app.data.repository.ActivityRepositoryImpl
import com.planner.app.data.repository.LlmRepositoryImpl
import com.planner.app.data.repository.LogRepositoryImpl
import com.planner.app.domain.repository.ActivityRepository
import com.planner.app.domain.repository.LlmRepository
import com.planner.app.domain.repository.LogRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindActivityRepository(impl: ActivityRepositoryImpl): ActivityRepository

    @Binds
    @Singleton
    abstract fun bindLogRepository(impl: LogRepositoryImpl): LogRepository

    @Binds
    @Singleton
    abstract fun bindLlmRepository(impl: LlmRepositoryImpl): LlmRepository
}
