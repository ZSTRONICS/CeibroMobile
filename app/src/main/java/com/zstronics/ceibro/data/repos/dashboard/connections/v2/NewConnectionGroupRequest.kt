package com.zstronics.ceibro.data.repos.dashboard.connections.v2


import com.google.gson.annotations.SerializedName

data class NewConnectionGroupRequest(
    @SerializedName("name")
    val name: String,
    @SerializedName("contacts")
    val contacts: List<String>
)