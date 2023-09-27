package com.zstronics.ceibro.data.database.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zstronics.ceibro.data.repos.task.models.v2.NewTaskV2Entity

class AssignedToStateTypeConverter {

    @TypeConverter
    fun fromString(value: String): List<NewTaskV2Entity.AssignedToStateNewEntity> {
        val type = object : TypeToken<List<NewTaskV2Entity.AssignedToStateNewEntity>>() {}.type
        return Gson().fromJson(value, type)
    }

    @TypeConverter
    fun fromList(list: List<NewTaskV2Entity.AssignedToStateNewEntity>): String {
        return Gson().toJson(list)
    }
}
