package com.zstronics.ceibro.data.database.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.zstronics.ceibro.data.database.models.tasks.AdvanceOptions
import com.zstronics.ceibro.data.database.models.tasks.TaskMember

class TaskMemberTypeConverter {

    @TypeConverter
    fun fromString(value: String): TaskMember {
        return Gson().fromJson(value, TaskMember::class.java)
    }

    @TypeConverter
    fun fromTaskMember(taskMember: TaskMember): String {
        return Gson().toJson(taskMember)
    }
}
