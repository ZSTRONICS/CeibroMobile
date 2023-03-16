package com.zstronics.ceibro.data.repos.projects.member

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import com.zstronics.ceibro.data.repos.chat.room.Member
import com.zstronics.ceibro.data.repos.projects.group.ProjectGroup
import com.zstronics.ceibro.data.repos.projects.role.ProjectRolesResponse

@Keep
data class GetProjectMemberResponse(
    @SerializedName("results")
    val members: List<ProjectMember>
) : BaseResponse() {
    @Keep
    data class ProjectMember(
        @SerializedName("createdAt")
        val createdAt: String,
        @SerializedName("creator")
        val creator: String,
        @SerializedName("group")
        val group: ProjectGroup?,
        @SerializedName("_id")
        val id: String,
        @SerializedName("isOwner")
        val isOwner: Boolean,
        @SerializedName("project")
        val project: String,
        @SerializedName("role")
        val role: ProjectRolesResponse.ProjectRole?,
        @SerializedName("updatedAt")
        val updatedAt: String,
        @SerializedName("user")
        val user: Member?
    ) : BaseResponse()
}