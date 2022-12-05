package com.zstronics.ceibro.data.repos.dashboard.connections


import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse

data class AllConnectionsResponse(
    @SerializedName("myConnections")
    val myConnections: List<MyConnection>
) : BaseResponse()