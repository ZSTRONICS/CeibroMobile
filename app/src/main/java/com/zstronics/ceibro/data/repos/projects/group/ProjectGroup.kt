package com.zstronics.ceibro.data.repos.projects.group

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import com.zstronics.ceibro.data.repos.chat.room.Member

@Keep
data class ProjectGroup(
    @SerializedName("createdAt")
    val createdAt: String = "",
    @SerializedName("_id")
    val id: String = "",
    @SerializedName("isDefaultGroup")
    val isDefaultGroup: Boolean = false,
    @SerializedName("members")
    val members: List<Member> = listOf(),
    @SerializedName("name")
    var name: String = "",
    @SerializedName("project")
    val project: String = "",
    @SerializedName("updatedAt")
    val updatedAt: String = ""
) : BaseResponse()