package com.zstronics.ceibro.data.repos.dashboard.connections


import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse

data class MyConnection(
    @SerializedName("from")
    val from: From,
    @SerializedName("id")
    val id: String,
    @SerializedName("sentByMe")
    val sentByMe: Boolean,
    @SerializedName("status")
    val status: String,
    @SerializedName("to")
    val to: To
) : BaseResponse()