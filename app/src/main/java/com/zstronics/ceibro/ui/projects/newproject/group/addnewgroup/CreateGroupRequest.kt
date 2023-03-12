package com.zstronics.ceibro.ui.projects.newproject.group.addnewgroup


import com.google.gson.annotations.SerializedName

data class CreateGroupRequest(
    @SerializedName("name")
    val name: String
)