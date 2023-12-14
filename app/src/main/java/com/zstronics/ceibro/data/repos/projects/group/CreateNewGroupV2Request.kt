package com.zstronics.ceibro.data.repos.projects.group


import com.google.gson.annotations.SerializedName

data class CreateNewGroupV2Request(
    @SerializedName("groupName")
    val groupName: String
)
