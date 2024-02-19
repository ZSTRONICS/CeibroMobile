package com.zstronics.ceibro.data.database.converters.v2

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.zstronics.ceibro.data.database.models.tasks.ProjectOfTask

class ProjectOfTaskTypeConverter {

    @TypeConverter
    fun fromString(value: String): ProjectOfTask? {
        return Gson().fromJson(value, ProjectOfTask::class.java)
    }

    @TypeConverter
    fun fromTaskMember(taskMemberDetail: ProjectOfTask?): String {
        return Gson().toJson(taskMemberDetail)
    }
}
