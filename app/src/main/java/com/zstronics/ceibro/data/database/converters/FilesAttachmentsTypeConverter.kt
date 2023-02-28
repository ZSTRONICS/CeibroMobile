package com.zstronics.ceibro.data.database.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.zstronics.ceibro.data.database.models.attachments.FilesAttachments

class FilesAttachmentsTypeConverter {

    @TypeConverter
    fun fromString(value: String): FilesAttachments? {
        return Gson().fromJson(value, FilesAttachments::class.java)
    }

    @TypeConverter
    fun fromFilesAttachments(filesAttachments: FilesAttachments?): String {
        return Gson().toJson(filesAttachments)
    }
}
