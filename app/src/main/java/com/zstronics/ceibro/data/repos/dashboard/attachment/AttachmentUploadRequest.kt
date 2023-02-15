package com.zstronics.ceibro.data.repos.dashboard.attachment


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class AttachmentUploadRequest(
    @SerializedName("files")
    val files: List<String>?,
    @SerializedName("moduleName")
    val moduleName: AttachmentModules,
    @SerializedName("_id")
    val _id: String
)