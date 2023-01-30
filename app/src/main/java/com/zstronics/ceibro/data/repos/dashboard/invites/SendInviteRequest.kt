package com.zstronics.ceibro.data.repos.dashboard.invites


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class SendInviteRequest(
    @SerializedName("email")
    val email: String? = ""
)