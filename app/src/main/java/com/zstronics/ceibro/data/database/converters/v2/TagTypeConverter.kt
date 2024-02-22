package com.zstronics.ceibro.data.database.converters.v2

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.zstronics.ceibro.data.database.models.tasks.Tag

class TagTypeConverter {

    @TypeConverter
    fun fromString(value: String): Tag? {
        return Gson().fromJson(value, Tag::class.java)
    }

    @TypeConverter
    fun fromTaskMember(data: Tag?): String {
        return Gson().toJson(data)
    }
}
