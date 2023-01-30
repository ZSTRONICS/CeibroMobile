package com.zstronics.ceibro.data.repos.auth.signup


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse

@Keep
data class GenericResponse(
    @SerializedName("code")
    val code: Int,
    @SerializedName("message")
    val message: String
): BaseResponse()