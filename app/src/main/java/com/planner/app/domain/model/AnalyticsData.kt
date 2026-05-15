package com.planner.app.domain.model

import java.time.LocalDate

data class HeatmapCell(
    val date: LocalDate,
    val intensity: Int,  // 0–4
)

data class ChartSeries(
    val label: String,
    val unit: String,
    val dataPoints: List<Pair<LocalDate, Double>>,
)

data class MissedDayPattern(
    val dayOfWeek: Int,   // 1=Mon…7=Sun
    val missRate: Float,  // 0.0–1.0
)

data class PeriodCount(
    val label: String,    // "Week", "Month", "3 months", "9 months", "12 months"
    val done: Int,
    val skip: Int,
)

data class AnalyticsData(
    val activityId: String,
    val currentStreak: Int,
    val longestStreak: Int,
    val periodCounts: List<PeriodCount>,
    val totalDone: Int,
    val totalLogged: Int,
    val heatmapCells: List<HeatmapCell>,
    val chartSeries: List<ChartSeries>,
    val missedDayPatterns: List<MissedDayPattern>,
    val averageDurationMinutes: Int,
)

data class ActivityCorrelation(
    val activityIdA: String,
    val activityIdB: String,
    val correlationScore: Float, // 0.0–1.0
    val description: String,
)

data class DashboardData(
    val momentumScore: Int,           // 0–100
    val timeAllocations: List<Pair<String, Float>>, // activityName → fraction
    val correlations: List<ActivityCorrelation>,
    val weeklyInsight: String?,
)
