package com.zstronics.ceibro.data.repos.auth.login


import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse

data class Refresh(
    @SerializedName("expires")
    val expires: String,
    @SerializedName("token")
    val token: String
):BaseResponse()