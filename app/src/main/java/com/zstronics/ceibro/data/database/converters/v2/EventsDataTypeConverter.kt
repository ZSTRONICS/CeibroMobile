package com.zstronics.ceibro.data.database.converters.v2

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.zstronics.ceibro.data.database.models.tasks.EventData
import com.zstronics.ceibro.data.database.models.tasks.TaskMemberDetail

class EventsDataTypeConverter {

    @TypeConverter
    fun fromString(value: String): EventData {
        return Gson().fromJson(value, EventData::class.java)
    }

    @TypeConverter
    fun fromTaskMember(taskMemberDetail: EventData): String {
        return Gson().toJson(taskMemberDetail)
    }
}
