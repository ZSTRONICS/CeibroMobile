package com.zstronics.ceibro.data.database.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.zstronics.ceibro.data.database.models.tasks.TaskProject

class TaskProjectTypeConverter {
    @TypeConverter
    fun fromString(value: String): TaskProject {
        return Gson().fromJson(value, TaskProject::class.java)
    }

    @TypeConverter
    fun fromTaskProject(taskProject: TaskProject): String {
        return Gson().toJson(taskProject)
    }
}
