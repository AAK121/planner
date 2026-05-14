package com.planner.app.domain.usecase.log

import com.planner.app.domain.model.ActivityLog
import com.planner.app.domain.repository.LogRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLogsForActivityUseCase @Inject constructor(
    private val logRepository: LogRepository,
) {
    operator fun invoke(activityId: String): Flow<List<ActivityLog>> =
        logRepository.observeForActivity(activityId)
}
