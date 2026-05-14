package com.planner.app.domain.usecase.activity

import com.planner.app.domain.model.Activity
import com.planner.app.domain.repository.ActivityRepository
import javax.inject.Inject

class CreateActivityUseCase @Inject constructor(
    private val activityRepository: ActivityRepository,
) {
    suspend operator fun invoke(activity: Activity) = activityRepository.create(activity)
}
