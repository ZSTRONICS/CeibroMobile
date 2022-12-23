package com.zstronics.ceibro.data.repos.dashboard.invites


import com.google.gson.annotations.SerializedName

data class SendInviteRequest(
    @SerializedName("email")
    val email: String? = ""
)