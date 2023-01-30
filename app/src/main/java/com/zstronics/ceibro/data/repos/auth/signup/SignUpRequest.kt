package com.zstronics.ceibro.data.repos.auth.signup


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class SignUpRequest(
    @SerializedName("email")
    val email: String,
    @SerializedName("firstName")
    val firstName: String,
    @SerializedName("password")
    val password: String,
    @SerializedName("surName")
    val surName: String
)