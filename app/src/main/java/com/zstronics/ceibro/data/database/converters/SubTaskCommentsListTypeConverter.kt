package com.zstronics.ceibro.data.database.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zstronics.ceibro.data.database.models.subtask.SubTaskComments
import com.zstronics.ceibro.data.database.models.tasks.TaskMember

class SubTaskCommentsListTypeConverter {

    @TypeConverter
    fun fromString(value: String?): ArrayList<SubTaskComments>? {
        val type = object : TypeToken<ArrayList<SubTaskComments>>() {}.type
        return Gson().fromJson(value, type)
    }

    @TypeConverter
    fun fromList(list: ArrayList<SubTaskComments>?): String? {
        return Gson().toJson(list)
    }
}
