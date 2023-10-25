package com.zstronics.ceibro.data.database.converters.v2

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zstronics.ceibro.data.database.models.tasks.AssignedToState

class AssignedToStateListTypeConverter {

    @TypeConverter
    fun fromString(value: String): ArrayList<AssignedToState> {
        val type = object : TypeToken<ArrayList<AssignedToState>>() {}.type
        return Gson().fromJson(value, type)
    }

    @TypeConverter
    fun fromList(list: ArrayList<AssignedToState>): String {
        return Gson().toJson(list)
    }
}
