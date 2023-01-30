package com.zstronics.ceibro.data.repos.dashboard.connections


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse

@Keep
data class AllConnectionsResponse(
    @SerializedName("myConnections")
    val myConnections: List<MyConnection>
) : BaseResponse()