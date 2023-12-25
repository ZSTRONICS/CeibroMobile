package com.zstronics.ceibro.data.database.converters.v2

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.zstronics.ceibro.data.database.models.tasks.EventData
import com.zstronics.ceibro.data.database.models.tasks.TaskMemberDetail
import com.zstronics.ceibro.data.repos.projects.drawing.DrawingV2

class DrawingV2TypeConverter {

    @TypeConverter
    fun fromString(value: String): DrawingV2? {
        return Gson().fromJson(value, DrawingV2::class.java)
    }

    @TypeConverter
    fun fromTaskMember(drawingV2: DrawingV2?): String {
        return Gson().toJson(drawingV2)
    }
}
