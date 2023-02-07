package com.zstronics.ceibro.data.database.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.zstronics.ceibro.data.database.models.subtask.SubTaskProject
import com.zstronics.ceibro.data.database.models.tasks.AdvanceOptions
import com.zstronics.ceibro.data.database.models.tasks.TaskMember

class SubTaskProjectTypeConverter {

    @TypeConverter
    fun fromString(value: String): SubTaskProject? {
        return Gson().fromJson(value, SubTaskProject::class.java)
    }

    @TypeConverter
    fun fromTaskMember(subTaskProject: SubTaskProject?): String {
        return Gson().toJson(subTaskProject)
    }
}
