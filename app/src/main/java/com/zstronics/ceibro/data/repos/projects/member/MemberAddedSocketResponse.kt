package com.zstronics.ceibro.data.repos.projects.member


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep
import com.zstronics.ceibro.data.database.models.attachments.FilesAttachments
import com.zstronics.ceibro.data.database.models.subtask.SubTaskComments

@Keep
data class MemberAddedSocketResponse(
    @SerializedName("data") val `data`: List<GetProjectMemberResponse.ProjectMember>,
    @SerializedName("eventType") val eventType: String,
    @SerializedName("module") val module: String
)