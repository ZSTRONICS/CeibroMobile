package com.zstronics.ceibro.data.database.converters.v2

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.zstronics.ceibro.data.database.models.tasks.CommentData
import com.zstronics.ceibro.data.database.models.tasks.Topic

class CommentDataTypeConverter {

    @TypeConverter
    fun fromString(value: String): CommentData? {
        return Gson().fromJson(value, CommentData::class.java)
    }

    @TypeConverter
    fun fromTaskMember(data: CommentData?): String {
        return Gson().toJson(data)
    }
}
