package com.zstronics.ceibro.data.database.converters.v2

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zstronics.ceibro.data.database.models.inbox.ActionFilesData
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.GroupContact

class GroupContactV2ListTypeConverter {

    @TypeConverter
    fun fromString(value: String): List<GroupContact>? {
        val type = object : TypeToken<List<GroupContact>>() {}.type
        return Gson().fromJson(value, type)
    }

    @TypeConverter
    fun fromList(list: List<GroupContact>?): String? {
        return Gson().toJson(list)
    }
}
