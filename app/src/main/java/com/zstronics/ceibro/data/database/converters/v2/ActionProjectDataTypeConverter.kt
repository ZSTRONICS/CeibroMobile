package com.zstronics.ceibro.data.database.converters.v2

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.zstronics.ceibro.data.database.models.inbox.ActionProjectData
import com.zstronics.ceibro.data.database.models.tasks.CeibroDrawingPins
import com.zstronics.ceibro.ui.locationv2.usage.AddLocationTask

class ActionProjectDataTypeConverter {

    @TypeConverter
    fun fromString(value: String): ActionProjectData? {
        return Gson().fromJson(value, ActionProjectData::class.java)
    }

    @TypeConverter
    fun fromAdvanceOptions(projectData: ActionProjectData?): String {
        return Gson().toJson(projectData)
    }
}
