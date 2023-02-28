package com.zstronics.ceibro.data.repos.task.models


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep
import com.zstronics.ceibro.data.database.models.attachments.FilesAttachments
import com.zstronics.ceibro.data.database.models.subtask.SubTaskComments

@Keep
data class CommentsFilesUploadedSocketEventResponse(
    @SerializedName("data") val `data`: SubTaskComments,
    @SerializedName("eventType") val eventType: String,
    @SerializedName("module") val module: String
)