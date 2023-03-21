package com.zstronics.ceibro.data.repos.projects.member


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep
import com.zstronics.ceibro.data.base.BaseResponse
import com.zstronics.ceibro.data.database.models.attachments.FilesAttachments
import com.zstronics.ceibro.data.database.models.subtask.SubTaskComments
import com.zstronics.ceibro.data.repos.projects.role.RefreshResponse

@Keep
data class MemberRefreshSocketResponse(
    @SerializedName("data") val `data`: RefreshResponse,
    @SerializedName("eventType") val eventType: String,
    @SerializedName("module") val module: String
): BaseResponse()