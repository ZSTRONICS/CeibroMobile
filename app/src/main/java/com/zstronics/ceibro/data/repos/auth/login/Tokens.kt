package com.zstronics.ceibro.data.repos.auth.login


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse

@Keep
data class Tokens(
    @SerializedName("access")
    val access: Access,
    @SerializedName("refresh")
    val refresh: Refresh
):BaseResponse()