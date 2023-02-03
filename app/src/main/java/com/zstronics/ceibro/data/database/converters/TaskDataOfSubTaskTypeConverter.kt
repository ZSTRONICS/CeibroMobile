package com.zstronics.ceibro.data.database.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.zstronics.ceibro.data.database.models.subtask.TaskDataOfSubTask

class TaskDataOfSubTaskTypeConverter {

    @TypeConverter
    fun fromString(value: String): TaskDataOfSubTask? {
        return Gson().fromJson(value, TaskDataOfSubTask::class.java)
    }

    @TypeConverter
    fun fromTaskMember(taskMember: TaskDataOfSubTask?): String {
        return Gson().toJson(taskMember)
    }
}
