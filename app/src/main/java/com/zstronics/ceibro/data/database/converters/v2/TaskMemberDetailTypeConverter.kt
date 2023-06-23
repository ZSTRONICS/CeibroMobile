package com.zstronics.ceibro.data.database.converters.v2

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.zstronics.ceibro.data.database.models.tasks.TaskMemberDetail

class TaskMemberDetailTypeConverter {

    @TypeConverter
    fun fromString(value: String): TaskMemberDetail {
        return Gson().fromJson(value, TaskMemberDetail::class.java)
    }

    @TypeConverter
    fun fromTaskMember(taskMemberDetail: TaskMemberDetail): String {
        return Gson().toJson(taskMemberDetail)
    }
}
