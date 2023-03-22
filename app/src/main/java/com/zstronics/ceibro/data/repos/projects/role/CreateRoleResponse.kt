package com.zstronics.ceibro.data.repos.projects.role


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import com.zstronics.ceibro.data.repos.chat.room.Member

@Keep
data class CreateRoleResponse(
    @SerializedName("result")
    val roles: UpdateProjectRole
) : BaseResponse() {
    @Keep
    data class UpdateProjectRole(
        @SerializedName("admin")
        val admin: Boolean,
        @SerializedName("createdAt")
        val createdAt: String,
        @SerializedName("_id")
        val id: String,
        @SerializedName("isDefaultRole")
        val isDefaultRole: Boolean,
        @SerializedName("memberPermission")
        val memberPermission: ProjectRolesResponse.ProjectRole.Permission,
        @SerializedName("members")
        val members: List<Member>,
        @SerializedName("name")
        val name: String,
        @SerializedName("permissions")
        val permissions: ProjectRolesResponse.ProjectRole.Permissions,
        @SerializedName("project")
        val project: String,
        @SerializedName("rolePermission")
        val rolePermission: ProjectRolesResponse.ProjectRole.Permission,
        @SerializedName("updatedAt")
        val updatedAt: String
    ) : BaseResponse()
}