package com.zstronics.ceibro.data.database.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ListConverters {
    @TypeConverter
    fun fromListToString(list: List<String>): String {
        return Gson().toJson(list)
    }

    @TypeConverter
    fun fromStringToList(string: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(string, listType)
    }
}