package com.zstronics.ceibro.data.repos.projects.group


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep
import com.zstronics.ceibro.data.database.models.attachments.FilesAttachments
import com.zstronics.ceibro.data.database.models.subtask.SubTaskComments

@Keep
data class GroupCreatedSocketResponse(
    @SerializedName("data") val `data`: ProjectGroup,
    @SerializedName("eventType") val eventType: String,
    @SerializedName("module") val module: String
)