package com.zstronics.ceibro.data.database.converters.v2

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zstronics.ceibro.data.database.models.projects.CeibroProjectV2
import com.zstronics.ceibro.data.repos.projects.projectsmain.AllProjectsResponseV2

class ProjectsV2ListTypeConverter {

    @TypeConverter
    fun fromString(value: String): List<CeibroProjectV2>? {
        val type = object : TypeToken<List<CeibroProjectV2>?>() {}.type
        return Gson().fromJson(value, type)
    }

    @TypeConverter
    fun fromList(list: List<CeibroProjectV2>?): String {
        return Gson().toJson(list)
    }
}
