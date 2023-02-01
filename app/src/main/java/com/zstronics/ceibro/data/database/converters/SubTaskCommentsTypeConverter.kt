package com.zstronics.ceibro.data.database.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.zstronics.ceibro.data.database.models.subtask.SubTaskComments

class SubTaskCommentsTypeConverter {

    @TypeConverter
    fun fromString(value: String): SubTaskComments? {
        return Gson().fromJson(value, SubTaskComments::class.java)
    }

    @TypeConverter
    fun fromTaskMember(taskMember: SubTaskComments?): String {
        return Gson().toJson(taskMember)
    }
}
