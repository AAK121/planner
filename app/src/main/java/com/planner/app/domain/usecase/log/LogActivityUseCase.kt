package com.planner.app.domain.usecase.log

import com.planner.app.domain.model.ActivityLog
import com.planner.app.domain.repository.LogRepository
import javax.inject.Inject

class LogActivityUseCase @Inject constructor(
    private val logRepository: LogRepository,
) {
    suspend operator fun invoke(log: ActivityLog) = logRepository.save(log)
}
