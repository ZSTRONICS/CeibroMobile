package com.zstronics.ceibro.data.database.converters.v2

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.zstronics.ceibro.data.database.models.tasks.Topic
import com.zstronics.ceibro.data.repos.projects.projectsmain.AllProjectsResponseV2

class OwnerV2TypeConverter {

    @TypeConverter
    fun fromString(value: String): AllProjectsResponseV2.ProjectsV2.OwnerV2? {
        return Gson().fromJson(value, AllProjectsResponseV2.ProjectsV2.OwnerV2::class.java)
    }

    @TypeConverter
    fun fromTaskMember(data: AllProjectsResponseV2.ProjectsV2.OwnerV2?): String {
        return Gson().toJson(data)
    }
}
