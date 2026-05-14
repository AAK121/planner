package com.planner.app.core.utils

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

object DateUtils {
    private val dayFmt = DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.getDefault())
    private val shortDayFmt = DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())

    fun LocalDate.toDisplayString(): String = format(dayFmt)
    fun LocalDate.toShortString(): String = format(shortDayFmt)

    fun dayOfWeekLabel(dayValue: Int, narrow: Boolean = false): String {
        val style = if (narrow) TextStyle.NARROW else TextStyle.SHORT
        return DayOfWeek.of(dayValue).getDisplayName(style, Locale.getDefault())
    }

    fun LocalDate.toEpochDay(): Long = toEpochDay()

    val DAY_LABELS_SHORT = (1..7).map { dayOfWeekLabel(it) }
    val DAY_LABELS_NARROW = (1..7).map { dayOfWeekLabel(it, narrow = true) }
}
