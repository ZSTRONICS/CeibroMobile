package com.zstronics.ceibro.data.repos.auth.login


import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse

data class Tokens(
    @SerializedName("access")
    val access: Access,
    @SerializedName("refresh")
    val refresh: Refresh
):BaseResponse()