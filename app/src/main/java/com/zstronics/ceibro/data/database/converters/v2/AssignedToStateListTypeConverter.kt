package com.zstronics.ceibro.data.database.converters.v2

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zstronics.ceibro.data.database.models.tasks.AssignedToState
import com.zstronics.ceibro.data.database.models.tasks.TaskMemberDetail

class AssignedToStateListTypeConverter {

    @TypeConverter
    fun fromString(value: String?): List<AssignedToState>? {
        val type = object : TypeToken<List<AssignedToState>?>() {}.type
        return Gson().fromJson(value, type)
    }

    @TypeConverter
    fun fromList(list: List<AssignedToState>?): String {
        return Gson().toJson(list)
    }
}
