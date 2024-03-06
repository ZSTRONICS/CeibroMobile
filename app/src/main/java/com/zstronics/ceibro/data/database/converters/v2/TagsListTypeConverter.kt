package com.zstronics.ceibro.data.database.converters.v2

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zstronics.ceibro.data.database.models.projects.CeibroProjectV2
import com.zstronics.ceibro.data.database.models.tasks.Tag

class TagsListTypeConverter {

    @TypeConverter
    fun fromString(value: String): List<Tag>? {
        val type = object : TypeToken<List<Tag>?>() {}.type
        return Gson().fromJson(value, type)
    }

    @TypeConverter
    fun fromList(list: List<Tag>?): String {
        return Gson().toJson(list)
    }
}
