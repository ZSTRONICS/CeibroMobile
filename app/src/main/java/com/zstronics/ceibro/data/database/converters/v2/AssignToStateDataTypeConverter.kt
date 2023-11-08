package com.zstronics.ceibro.data.database.converters.v2

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.zstronics.ceibro.data.database.models.tasks.AssignedToState
import com.zstronics.ceibro.data.database.models.tasks.EventData
import com.zstronics.ceibro.data.database.models.tasks.TaskMemberDetail

class AssignToStateDataTypeConverter {

    @TypeConverter
    fun fromString(value: String): AssignedToState? {
        return Gson().fromJson(value, AssignedToState::class.java)
    }

    @TypeConverter
    fun fromTaskMember(taskMemberDetail: AssignedToState?): String {
        return Gson().toJson(taskMemberDetail)
    }
}
