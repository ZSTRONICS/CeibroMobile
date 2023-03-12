package com.zstronics.ceibro.data.repos.projects.group


import com.google.gson.annotations.SerializedName

data class CreateGroupRequest(
    @SerializedName("name")
    val name: String
)