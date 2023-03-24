package com.zstronics.ceibro.data.repos.auth.login


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse

@Keep
data class UserProfilePicUpdateResponse(
    @SerializedName("profilePic")
    var profilePic: String,
) : BaseResponse()