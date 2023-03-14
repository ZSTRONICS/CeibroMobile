package com.zstronics.ceibro.data.repos.projects.member


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse

@Keep
data class DeleteMemberResponse(
    @SerializedName("message")
    val message: String
) : BaseResponse()