package com.planner.app.feature.widget

import com.planner.app.domain.model.LogStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

/**
 * In-memory map of `activityId -> optimistically-applied LogStatus` for the
 * widget. The widget overlays these on top of the actual DB-derived logs so
 * a tap flips the row visually within ~100 ms (no waiting for DB write +
 * Room emission + launcher rebind).
 *
 * Entries auto-clear once the widget observes a real DB log with the same
 * status (handled in the widget composable via a LaunchedEffect).
 */
@Singleton
class OptimisticToggles @Inject constructor() {

    private val _state = MutableStateFlow<Map<String, LogStatus>>(emptyMap())
    val state: StateFlow<Map<String, LogStatus>> = _state

    fun set(activityId: String, status: LogStatus) {
        _state.update { it + (activityId to status) }
    }

    fun get(activityId: String): LogStatus? = _state.value[activityId]

    fun clear(activityIds: Collection<String>) {
        if (activityIds.isEmpty()) return
        _state.update { it - activityIds }
    }
}
