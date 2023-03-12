package com.zstronics.ceibro.data.repos.projects.member


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class CreateProjectMemberRequest(
    @SerializedName("group")
    val group: String,
    @SerializedName("role")
    val role: String,
    @SerializedName("user")
    val user: List<String>?
)