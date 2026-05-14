package com.planner.app.domain.usecase.activity

import com.planner.app.domain.repository.ActivityRepository
import javax.inject.Inject

class DeleteActivityUseCase @Inject constructor(
    private val activityRepository: ActivityRepository,
) {
    suspend operator fun invoke(id: String) = activityRepository.delete(id)
}
