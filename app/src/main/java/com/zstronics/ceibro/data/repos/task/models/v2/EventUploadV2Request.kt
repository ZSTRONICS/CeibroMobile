package com.zstronics.ceibro.data.repos.task.models.v2


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import java.io.File

@Keep
data class EventWithFileUploadV2Request(
    @SerializedName("files")
    val files: List<File>?,
    @SerializedName("message")
    val message: String,
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


@Keep
data class EventCommentOnlyUploadV2Request(
    @SerializedName("message")
    val message: String
)