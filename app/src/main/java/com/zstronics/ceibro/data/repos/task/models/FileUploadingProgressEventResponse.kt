package com.zstronics.ceibro.data.repos.task.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import com.zstronics.ceibro.data.database.models.attachments.FilesAttachments

@Keep

data class FileUploadingProgressEventResponse(
    @SerializedName("eventType")
    val eventType: String,
    @SerializedName("data")
    val data: FileUploadProgressData?
) : BaseResponse() {
    @Keep
    data class FileUploadProgressData(
        @SerializedName("file")
        val file: FilesAttachments?,
        @SerializedName("fileId") val fileId: String,
        @SerializedName("fileName") val fileName: String,
        @SerializedName("moduleId") val moduleId: String,
        @SerializedName("moduleType") val moduleType: String,
        @SerializedName("progress") val progress: Int,
        @SerializedName("totalSize") val totalSize: Int,
        @SerializedName("uploadedSize") val uploadedSize: Int
    )
}
