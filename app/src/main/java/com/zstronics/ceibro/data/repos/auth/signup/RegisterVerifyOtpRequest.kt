package com.zstronics.ceibro.data.repos.auth.signup


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class RegisterVerifyOtpRequest(
    @SerializedName("phoneNumber")
    val phoneNumber: String,
    @SerializedName("otp")
    val otp: String
)