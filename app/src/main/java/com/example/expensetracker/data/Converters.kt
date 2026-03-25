package com.example.expensetracker.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromStringToMap(value: String?): Map<String, Double> {
        if (value == null) {
            return emptyMap()
        }
        val mapType = object : TypeToken<Map<String, Double>>() {}.type
        return gson.fromJson(value, mapType)
    }

    @TypeConverter
    fun fromMapToString(map: Map<String, Double>?): String {
        if (map == null) {
            return ""
        }
        return gson.toJson(map)
    }
}
