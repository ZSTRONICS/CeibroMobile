package com.zstronics.ceibro.data.repos.auth.login


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep
import com.zstronics.ceibro.data.database.models.attachments.FilesAttachments
import com.zstronics.ceibro.data.database.models.subtask.SubTaskComments

@Keep
data class UserUpdatedSocketResponse(
    @SerializedName("data") val `data`: User,
    @SerializedName("eventType") val eventType: String,
    @SerializedName("module") val module: String
)