package com.planner.app.core.utils

import java.util.UUID

fun newId(): String = UUID.randomUUID().toString()

fun Int.minutesToDisplay(): String {
    val h = this / 60
    val m = this % 60
    return when {
        h == 0 -> "${m}m"
        m == 0 -> "${h}h"
        else   -> "${h}h ${m}m"
    }
}

fun Float.toPercent(): String = "${(this * 100).toInt()}%"

fun String.capitalizeFirst(): String =
    if (isEmpty()) this else this[0].uppercaseChar() + substring(1).lowercase()
