package com.zstronics.ceibro.data.repos.task.models.v2




import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import java.io.File

@Keep
data class ApproveOrRejectTaskRequest(
    @SerializedName("files")
    val files: List<File>?,
    @SerializedName("message")
    val message: String,
    @SerializedName("metadata")
    val metadata: String,
    @SerializedName("hasFiles")
    val hasFiles:Boolean,
    @SerializedName("approvalType")
    val approvalEvent:String
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
