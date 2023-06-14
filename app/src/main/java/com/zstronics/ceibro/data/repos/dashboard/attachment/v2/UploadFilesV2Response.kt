package com.zstronics.ceibro.data.repos.dashboard.attachment.v2


import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse

data class UploadFilesV2Response(
    @SerializedName("data")
    val uploadData: List<UploadedFile>
) : BaseResponse() {
    data class UploadedFile(
        @SerializedName("access")
        val access: List<String>,
        @SerializedName("comment")
        val comment: String,
        @SerializedName("createdAt")
        val createdAt: String,
        @SerializedName("fileName")
        val fileName: String,
        @SerializedName("fileTag")
        val fileTag: String,
        @SerializedName("fileType")
        val fileType: String,
        @SerializedName("fileUrl")
        val fileUrl: String,
        @SerializedName("hasComment")
        val hasComment: Boolean,
        @SerializedName("_id")
        val id: String,
        @SerializedName("moduleId")
        val moduleId: String,
        @SerializedName("moduleType")
        val moduleType: String,
        @SerializedName("updatedAt")
        val updatedAt: String,
        @SerializedName("uploadStatus")
        val uploadStatus: String,
        @SerializedName("uploadedBy")
        val uploadedBy: String,
        @SerializedName("__v")
        val v: Int,
        @SerializedName("version")
        val version: Int
    ) : BaseResponse()
}