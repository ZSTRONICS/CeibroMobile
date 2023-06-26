package com.zstronics.ceibro.data.database.converters.v2

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zstronics.ceibro.data.database.models.tasks.Events
import com.zstronics.ceibro.data.database.models.tasks.Files
import com.zstronics.ceibro.data.database.models.tasks.InvitedNumbers

class InvitedNumbersListTypeConverter {

    @TypeConverter
    fun fromString(value: String): List<InvitedNumbers> {
        val type = object : TypeToken<List<InvitedNumbers>>() {}.type
        return Gson().fromJson(value, type)
    }

    @TypeConverter
    fun fromList(list: List<InvitedNumbers>): String {
        return Gson().toJson(list)
    }
}
