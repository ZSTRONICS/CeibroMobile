package com.zstronics.ceibro.data.repos.auth.refreshtoken


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class RefreshTokenRequest(
    @SerializedName("refreshToken")
    val refreshToken: String
)