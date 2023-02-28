package com.zstronics.ceibro.data.database.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zstronics.ceibro.data.database.models.attachments.FilesAttachments

class FilesAttachmentsListTypeConverter {

    @TypeConverter
    fun fromString(value: String): ArrayList<FilesAttachments>? {
        val type = object : TypeToken<ArrayList<FilesAttachments>?>() {}.type
        return Gson().fromJson(value, type)
    }

    @TypeConverter
    fun fromList(list: ArrayList<FilesAttachments>?): String? {
        return Gson().toJson(list)
    }
}
