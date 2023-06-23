package com.zstronics.ceibro.data.database.converters.v2

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.AllCeibroConnections

class CeibroConnectionListTypeConverter {

    @TypeConverter
    fun fromString(value: String): List<AllCeibroConnections.CeibroConnection> {
        val type = object : TypeToken<List<AllCeibroConnections.CeibroConnection>>() {}.type
        return Gson().fromJson(value, type)
    }

    @TypeConverter
    fun fromList(list: List<AllCeibroConnections.CeibroConnection>): String {
        return Gson().toJson(list)
    }
}
