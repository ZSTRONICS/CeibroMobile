package com.zstronics.ceibro.data.database.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zstronics.ceibro.data.database.models.subtask.RejectionComment
import com.zstronics.ceibro.data.database.models.subtask.SubTaskStateItem
import com.zstronics.ceibro.data.database.models.subtask.Viewer

class SubTaskRejectionCommentsListTypeConverter {

    @TypeConverter
    fun fromString(value: String): List<RejectionComment>? {
        val type = object : TypeToken<List<RejectionComment>>() {}.type
        return Gson().fromJson(value, type)
    }

    @TypeConverter
    fun fromList(list: List<RejectionComment>?): String? {
        return Gson().toJson(list)
    }
}
