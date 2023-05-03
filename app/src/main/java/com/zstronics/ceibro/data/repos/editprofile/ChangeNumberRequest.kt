package com.zstronics.ceibro.data.repos.editprofile


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ChangeNumberRequest(
    @SerializedName("newNumber")
    val newNumber: String,
    @SerializedName("countryCode")
    val countryCode: String,
    @SerializedName("password")
    val password: String
)