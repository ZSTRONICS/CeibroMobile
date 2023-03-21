package com.zstronics.ceibro.data.repos.projects.role

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse

@Keep
data class RefreshResponse(
    @SerializedName("projectId")
    val projectId: String
) : BaseResponse()
