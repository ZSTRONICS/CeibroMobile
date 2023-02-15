package com.zstronics.ceibro.data.repos.task.models


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep
import com.zstronics.ceibro.data.database.models.attachments.FilesAttachments

@Keep
data class AllFilesUploadedSocketEventResponse(
    @SerializedName("data") val `data`: Data,
    @SerializedName("eventType") val eventType: String,
    @SerializedName("module") val module: String
) {
    @Keep
    data class Data(
        @SerializedName("files") val files: List<FilesAttachments>,
        @SerializedName("moduleId") val moduleId: String,
        @SerializedName("moduleName") val moduleName: String
    )
}