package com.zstronics.ceibro.data.repos.dashboard.contacts


import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse

data class GetContactsResponse(
    @SerializedName("message")
    val message: String
) : BaseResponse()