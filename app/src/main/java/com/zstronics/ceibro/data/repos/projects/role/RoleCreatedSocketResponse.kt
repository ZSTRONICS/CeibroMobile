package com.zstronics.ceibro.data.repos.projects.role


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep
import com.zstronics.ceibro.data.database.models.attachments.FilesAttachments
import com.zstronics.ceibro.data.database.models.subtask.SubTaskComments

@Keep
data class RoleCreatedSocketResponse(
    @SerializedName("data") val `data`: ProjectRolesResponse.ProjectRole,
    @SerializedName("eventType") val eventType: String,
    @SerializedName("module") val module: String
)