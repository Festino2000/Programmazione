package com.example.mobileapp.areaPersonale

import androidx.room.TypeConverter

class UriListConverter {
    @TypeConverter
    fun fromList(list: List<String>): String {
        return list.joinToString(separator = "|")
    }

    @TypeConverter
    fun toList(data: String): List<String> {
        return if (data.isEmpty()) emptyList() else data.split("|")
    }
}
