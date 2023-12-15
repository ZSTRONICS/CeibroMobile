package com.zstronics.ceibro.data.database.converters.v2

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zstronics.ceibro.data.database.models.projects.CeibroFloorV2
import com.zstronics.ceibro.data.database.models.projects.CeibroProjectV2

class FloorsV2ListTypeConverter {

    @TypeConverter
    fun fromString(value: String): List<CeibroFloorV2>? {
        val type = object : TypeToken<List<CeibroFloorV2>?>() {}.type
        return Gson().fromJson(value, type)
    }

    @TypeConverter
    fun fromList(list: List<CeibroFloorV2>?): String {
        return Gson().toJson(list)
    }
}
