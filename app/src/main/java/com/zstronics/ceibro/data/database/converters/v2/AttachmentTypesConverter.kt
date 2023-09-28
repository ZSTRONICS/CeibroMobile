package com.zstronics.ceibro.data.database.converters.v2

import androidx.room.TypeConverter
import com.google.gson.Gson
import ee.zstronics.ceibro.camera.AttachmentTypes

class AttachmentTypesConverter {
    @TypeConverter
    fun fromString(value: String): AttachmentTypes {
        return Gson().fromJson(value, AttachmentTypes::class.java)
    }

    @TypeConverter
    fun fromTaskMember(data: AttachmentTypes): String {
        return Gson().toJson(data)
    }
}