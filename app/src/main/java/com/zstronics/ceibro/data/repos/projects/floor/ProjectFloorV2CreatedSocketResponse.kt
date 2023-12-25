package com.zstronics.ceibro.data.repos.projects.floor


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep
import com.zstronics.ceibro.data.base.BaseResponse
import com.zstronics.ceibro.data.database.models.attachments.FilesAttachments
import com.zstronics.ceibro.data.database.models.projects.CeibroFloorV2
import com.zstronics.ceibro.data.database.models.subtask.SubTaskComments
import com.zstronics.ceibro.data.repos.projects.role.RefreshResponse

@Keep
data class ProjectFloorV2CreatedSocketResponse(
    @SerializedName("data") val `data`: CeibroFloorV2,
    @SerializedName("eventType") val eventType: String,
    @SerializedName("module") val module: String
): BaseResponse()