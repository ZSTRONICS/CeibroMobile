package com.zstronics.ceibro.data.repos.dashboard.attachment.v2


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import java.io.File

@Keep
data class AttachmentUploadV2Request(
    @SerializedName("files")
    val files: List<File>?,
    @SerializedName("moduleName")
    val moduleName: String,
    @SerializedName("moduleId")
    val moduleId: String,
    @SerializedName("metadata")
    val metadata: String
) {
    @Keep
    data class AttachmentMetaData(
        @SerializedName("fileName")
        val fileName: String,
        @SerializedName("orignalFileName")
        val orignalFileName: String,
        @SerializedName("tag")
        val tag: String,
        @SerializedName("comment")
        val comment: String,
    )
}