package com.zstronics.ceibro.data.repos.projects.role


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import com.zstronics.ceibro.data.repos.chat.room.Member

@Keep
data class ProjectRolesResponse(
    @SerializedName("result")
    val roles: List<ProjectRole>
) : BaseResponse() {
    @Keep
    data class ProjectRole(
        @SerializedName("admin")
        val admin: Boolean,
        @SerializedName("createdAt")
        val createdAt: String,
        @SerializedName("_id")
        val id: String,
        @SerializedName("isDefaultRole")
        val isDefaultRole: Boolean,
        @SerializedName("memberPermission")
        val memberPermission: Permission,
        @SerializedName("members")
        val members: List<Member> = listOf(),
        @SerializedName("name")
        val name: String,
        @SerializedName("permissions")
        val permissions: Permissions,
        @SerializedName("project")
        val project: String,
        @SerializedName("rolePermission")
        val rolePermission: Permission,
        @SerializedName("updatedAt")
        val updatedAt: String
    ) : BaseResponse() {
        @Keep
        data class Permissions(
            @SerializedName("admin")
            val admin: Admin,
            @SerializedName("individual")
            val individual: Individual,
            @SerializedName("subContractor")
            val subContractor: SubContractor
        ) : BaseResponse() {
            @Keep
            data class Admin(
                @SerializedName("member")
                val member: Permission,
                @SerializedName("roles")
                val roles: Permission,
                @SerializedName("timeProfile")
                val timeProfile: Permission
            ) : BaseResponse()

            @Keep
            data class Individual(
                @SerializedName("member")
                val member: Permission,
                @SerializedName("roles")
                val roles: Permission,
                @SerializedName("timeProfile")
                val timeProfile: Permission
            ) : BaseResponse()

            @Keep
            data class SubContractor(
                @SerializedName("member")
                val member: Permission,
                @SerializedName("timeProfile")
                val timeProfile: Permission
            ) : BaseResponse()
        }

        @Keep
        data class Permission(
            @SerializedName("create")
            val create: Boolean,
            @SerializedName("delete")
            val delete: Boolean,
            @SerializedName("edit")
            val edit: Boolean
        ) : BaseResponse()
    }
}