package com.zstronics.ceibro.data.database.converters.v2

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.zstronics.ceibro.data.database.models.tasks.Topic

class TopicTypeConverter {

    @TypeConverter
    fun fromString(value: String): Topic? {
        return Gson().fromJson(value, Topic::class.java)
    }

    @TypeConverter
    fun fromTaskMember(data: Topic?): String {
        return Gson().toJson(data)
    }
}
