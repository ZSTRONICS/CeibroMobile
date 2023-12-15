package com.zstronics.ceibro.data.database.converters.v2

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zstronics.ceibro.data.database.models.projects.CeibroFloorV2
import com.zstronics.ceibro.data.database.models.projects.CeibroGroupsV2
import com.zstronics.ceibro.data.database.models.projects.CeibroProjectV2

class GroupsV2ListTypeConverter {

    @TypeConverter
    fun fromString(value: String): List<CeibroGroupsV2>? {
        val type = object : TypeToken<List<CeibroGroupsV2>?>() {}.type
        return Gson().fromJson(value, type)
    }

    @TypeConverter
    fun fromList(list: List<CeibroGroupsV2>?): String {
        return Gson().toJson(list)
    }
}
