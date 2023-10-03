package com.zstronics.ceibro.data.database.converters.v2

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.zstronics.ceibro.data.repos.task.models.SingleTaskEntity
import com.zstronics.ceibro.data.repos.task.models.TaskV2Response

class CeibroSingleTaskEntityV2TypeConverter {

    @TypeConverter
    fun fromString(value: String): SingleTaskEntity {
        return Gson().fromJson(value, SingleTaskEntity::class.java)
    }

    @TypeConverter
    fun fromTaskMember(data: SingleTaskEntity): String {
        return Gson().toJson(data)
    }
}
