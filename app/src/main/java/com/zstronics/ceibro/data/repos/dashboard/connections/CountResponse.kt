package com.zstronics.ceibro.data.repos.dashboard.connections


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse

@Keep
data class CountResponse(
    @SerializedName("count")
    val count: Int
) : BaseResponse()