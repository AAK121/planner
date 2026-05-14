package com.planner.app.feature.activities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.planner.app.domain.model.Activity
import com.planner.app.domain.repository.ActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ActivitiesUiState(
    val activities: List<Activity> = emptyList(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class ActivitiesViewModel @Inject constructor(
    private val activityRepository: ActivityRepository,
) : ViewModel() {

    val uiState: StateFlow<ActivitiesUiState> = activityRepository.observeAll()
        .map { ActivitiesUiState(activities = it, isLoading = false) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, ActivitiesUiState())

    fun delete(activityId: String) {
        viewModelScope.launch {
            activityRepository.delete(activityId)
        }
    }
}
