package com.zstronics.ceibro.data.repos.auth.refreshtoken


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse

@Keep
data class TokenValidityResponse(
    @SerializedName("isValid")
    val isValid: Boolean
): BaseResponse()