package com.zstronics.ceibro.data.database.converters.v2

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.AllCeibroConnections

class UserCeibroDataTypeConverter {

    @TypeConverter
    fun fromString(value: String): AllCeibroConnections.CeibroConnection.UserCeibroData? {
        return Gson().fromJson(
            value,
            AllCeibroConnections.CeibroConnection.UserCeibroData::class.java
        )
    }

    @TypeConverter
    fun fromTaskMember(data: AllCeibroConnections.CeibroConnection.UserCeibroData?): String {
        return Gson().toJson(data)
    }
}
