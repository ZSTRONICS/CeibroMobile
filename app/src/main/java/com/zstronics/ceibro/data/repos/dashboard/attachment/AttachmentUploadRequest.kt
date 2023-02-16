package com.zstronics.ceibro.data.repos.dashboard.attachment


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import java.io.File

@Keep
data class AttachmentUploadRequest(
    @SerializedName("files")
    val files: List<File>?,
    @SerializedName("moduleName")
    val moduleName: AttachmentModules,
    @SerializedName("_id")
    val _id: String
)