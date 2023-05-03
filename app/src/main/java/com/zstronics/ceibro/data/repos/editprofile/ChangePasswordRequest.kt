package com.zstronics.ceibro.data.repos.editprofile


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ChangePasswordRequest(
    @SerializedName("oldPassword")
    val oldPassword: String,
    @SerializedName("newPassword")
    val newPassword: String
)