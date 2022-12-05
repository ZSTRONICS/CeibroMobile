package com.zstronics.ceibro.data.repos.auth.login


import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse

data class LoginResponse(
    @SerializedName("tokens")
    val tokens: Tokens,
    @SerializedName("user")
    val user: User
):BaseResponse()