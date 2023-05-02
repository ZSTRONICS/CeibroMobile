package com.zstronics.ceibro.data.repos.auth.login


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class LoginRequest(
    @SerializedName("phoneNumber")
    val phoneNumber: String? = "",
    @SerializedName("password")
    val password: String = "null"
)