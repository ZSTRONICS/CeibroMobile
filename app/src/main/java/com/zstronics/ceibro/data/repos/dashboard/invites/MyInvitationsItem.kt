package com.zstronics.ceibro.data.repos.dashboard.invites


import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse

data class MyInvitationsItem(
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("from")
    val from: From,
    @SerializedName("_id")
    val id: String,
    @SerializedName("status")
    val status: String
) : BaseResponse()