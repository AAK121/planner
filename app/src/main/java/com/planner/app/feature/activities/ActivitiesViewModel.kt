package com.planner.app.feature.activities

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.planner.app.domain.model.Activity
import com.planner.app.domain.repository.ActivityRepository
import com.planner.app.feature.widget.TodayActivitiesWidget
import com.planner.app.notifications.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
    private val reminderScheduler: ReminderScheduler,
    @ApplicationContext private val appContext: Context,
) : ViewModel() {

    val uiState: StateFlow<ActivitiesUiState> = activityRepository.observeAll()
        .map { ActivitiesUiState(activities = it, isLoading = false) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, ActivitiesUiState())

    fun delete(activityId: String) {
        viewModelScope.launch {
            // 1) Look up the activity FIRST so we can cancel its pending alarms.
            //    Once it's deleted from the DB its timesOfDay are lost.
            val activity = activityRepository.getById(activityId)
            if (activity != null) {
                reminderScheduler.cancelForActivity(activity)
            }
            // 2) Delete from DB.
            activityRepository.delete(activityId)
            // 3) Force the widget to refresh so the removed row disappears.
            TodayActivitiesWidget().updateAll(appContext)
        }
    }
}
