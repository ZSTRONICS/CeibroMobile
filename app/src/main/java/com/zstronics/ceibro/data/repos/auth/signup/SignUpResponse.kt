package com.zstronics.ceibro.data.repos.auth.signup


import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse

data class SignUpResponse(
    @SerializedName("code")
    val code: Int,
    @SerializedName("message")
    val message: String
): BaseResponse()