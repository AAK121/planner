package com.planner.app.domain.model

enum class ActivityType {
    RECURRING,   // fixed schedule, streaks + compliance tracked
    OCCASIONAL,  // no fixed schedule, frequency tracked
    ONE_OFF,     // single event, calendar visibility only
}
