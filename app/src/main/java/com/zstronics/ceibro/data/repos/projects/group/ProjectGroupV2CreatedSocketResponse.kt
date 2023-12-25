package com.zstronics.ceibro.data.repos.projects.group


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import com.zstronics.ceibro.data.database.models.projects.CeibroGroupsV2

@Keep
data class ProjectGroupV2CreatedSocketResponse(
    @SerializedName("data") val `data`: CeibroGroupsV2,
    @SerializedName("eventType") val eventType: String,
    @SerializedName("module") val module: String
): BaseResponse()