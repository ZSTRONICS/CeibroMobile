package com.zstronics.ceibro.data.repos.dashboard.connections.v2


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse

@Keep
data class GetConnectionGroupsResponse(
    @SerializedName("groups")
    val groups: List<CeibroConnectionGroupV2>
) : BaseResponse()