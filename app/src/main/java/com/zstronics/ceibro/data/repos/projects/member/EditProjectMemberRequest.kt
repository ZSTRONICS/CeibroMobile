package com.zstronics.ceibro.data.repos.projects.member


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class EditProjectMemberRequest(
    @SerializedName("groupId")
    val group: String,
    @SerializedName("roleId")
    val role: String
)