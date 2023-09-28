package com.zstronics.ceibro.data.database.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zstronics.ceibro.data.repos.task.models.v2.LocalFilesToStore

class LocalFilesToStoreTypeConverter {

    @TypeConverter
    fun fromString(value: String): List<LocalFilesToStore> {
        val type = object : TypeToken<List<LocalFilesToStore>>() {}.type
        return Gson().fromJson(value, type)
    }

    @TypeConverter
    fun fromList(list: List<LocalFilesToStore>): String {
        return Gson().toJson(list)
    }
}
