package com.zstronics.ceibro.data.repos.projects.group


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse

@Keep
data class ProjectGroupV2DeletedSocketResponse(
    @SerializedName("data") val `data`: String,
    @SerializedName("eventType") val eventType: String,
    @SerializedName("module") val module: String
): BaseResponse()