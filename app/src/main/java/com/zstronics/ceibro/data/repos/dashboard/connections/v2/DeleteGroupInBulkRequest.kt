package com.zstronics.ceibro.data.repos.dashboard.connections.v2

import com.google.gson.annotations.SerializedName

data class DeleteGroupInBulkRequest(
    @SerializedName("groupIds")
    val groupIds: List<String>
)