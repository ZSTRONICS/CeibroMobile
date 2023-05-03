package com.zstronics.ceibro.data.repos.editprofile


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import com.zstronics.ceibro.data.repos.auth.login.User

@Keep
data class EditProfileResponse(
    @SerializedName("user")
    val user: User
): BaseResponse()