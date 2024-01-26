package com.zstronics.ceibro.data.database.converters.v2

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.zstronics.ceibro.data.database.models.inbox.ActionDataTask
import com.zstronics.ceibro.data.database.models.inbox.ActionProjectData
import com.zstronics.ceibro.data.database.models.tasks.CeibroDrawingPins
import com.zstronics.ceibro.ui.locationv2.usage.AddLocationTask

class ActionDataTaskTypeConverter {

    @TypeConverter
    fun fromString(value: String): ActionDataTask? {
        return Gson().fromJson(value, ActionDataTask::class.java)
    }

    @TypeConverter
    fun fromAdvanceOptions(taskData: ActionDataTask?): String {
        return Gson().toJson(taskData)
    }
}
