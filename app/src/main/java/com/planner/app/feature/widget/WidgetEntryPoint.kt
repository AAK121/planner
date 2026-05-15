package com.planner.app.feature.widget

import com.planner.app.domain.repository.LogRepository
import com.planner.app.domain.usecase.activity.GetTodayActivitiesUseCase
import com.planner.app.domain.usecase.log.LogActivityUseCase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt entry point for the Glance widget code. Glance's `GlanceAppWidget` and
 * `ActionCallback` aren't compatible with `@AndroidEntryPoint` since they aren't
 * Activities/Services, so we pull dependencies via `EntryPointAccessors`.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun getTodayActivities(): GetTodayActivitiesUseCase
    fun logRepository(): LogRepository
    fun logActivity(): LogActivityUseCase
    fun optimisticToggles(): OptimisticToggles
}
