package com.zstronics.ceibro.data.repos.projects.drawing


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import com.zstronics.ceibro.data.database.models.projects.CeibroGroupsV2

@Keep
data class ProjectDrawingUploadedSocketResponse(
    @SerializedName("data") val `data`: UploadedFileResponse,
    @SerializedName("eventType") val eventType: String,
    @SerializedName("module") val module: String
): BaseResponse()

data class UploadedFileResponse(
    @SerializedName("drawings")
    val drawings: List<DrawingV2>,
    @SerializedName("floorId")
    val floorId: String,
    @SerializedName("floorUpdatedAt")
    val floorUpdatedAt: String,
    @SerializedName("groupId")
    val groupId: String,
    @SerializedName("groupUpdatedAt")
    val groupUpdatedAt: String,
    @SerializedName("message")
    val message: String,
    @SerializedName("projectId")
    val projectId: String
)
