package com.planner.app.data.local.database.converter

import androidx.room.TypeConverter

class Converters {
    @TypeConverter fun fromLong(value: Long?): Long? = value
    @TypeConverter fun toLong(value: Long?): Long? = value
}
