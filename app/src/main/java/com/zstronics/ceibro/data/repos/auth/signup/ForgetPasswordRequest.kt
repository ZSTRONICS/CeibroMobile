package com.zstronics.ceibro.data.repos.auth.signup


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ForgetPasswordRequest(
    @SerializedName("phoneNumber")
    val phoneNumber: String
)