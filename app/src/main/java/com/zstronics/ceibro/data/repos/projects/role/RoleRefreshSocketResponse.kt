package com.zstronics.ceibro.data.repos.projects.role


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep
import com.zstronics.ceibro.data.base.BaseResponse
import com.zstronics.ceibro.data.database.models.attachments.FilesAttachments
import com.zstronics.ceibro.data.database.models.subtask.SubTaskComments

@Keep
data class RoleRefreshSocketResponse(
    @SerializedName("data") val `data`: RefreshRoleResponse,
    @SerializedName("eventType") val eventType: String,
    @SerializedName("module") val module: String
): BaseResponse() {
    @Keep
    data class RefreshRoleResponse (
        @SerializedName("projectId")
        val projectId: String
    ) : BaseResponse()
}