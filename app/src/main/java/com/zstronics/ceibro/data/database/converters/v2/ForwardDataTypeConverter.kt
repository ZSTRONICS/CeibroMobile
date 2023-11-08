package com.zstronics.ceibro.data.database.converters.v2

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.zstronics.ceibro.data.database.models.tasks.ForwardData

class ForwardDataTypeConverter {

    @TypeConverter
    fun fromString(value: String): ForwardData? {
        return Gson().fromJson(value, ForwardData::class.java)
    }

    @TypeConverter
    fun fromTaskMember(data: ForwardData?): String {
        return Gson().toJson(data)
    }
}
