package com.zstronics.ceibro.data.repos.editprofile


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ChangeNumberVerifyOtpRequest(
    @SerializedName("newNumber")
    val newNumber: String,
    @SerializedName("otp")
    val otp: String
)