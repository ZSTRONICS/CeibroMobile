package com.zstronics.ceibro.data.repos.projects.role


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class CreateRoleRequest(
    @SerializedName("admin")
    val admin: Boolean,
    @SerializedName("memberPermission")
    val memberPermission: PermissionRequest,
    @SerializedName("members")
    val members: List<String>? = listOf(),
    @SerializedName("name")
    var name: String,
    @SerializedName("project")
    val project: String,
    @SerializedName("rolePermission")
    val rolePermission: PermissionRequest
) {
    @Keep
    data class PermissionRequest(
        @SerializedName("create")
        val create: Boolean,
        @SerializedName("delete")
        val delete: Boolean,
        @SerializedName("edit")
        val edit: Boolean
    )

}