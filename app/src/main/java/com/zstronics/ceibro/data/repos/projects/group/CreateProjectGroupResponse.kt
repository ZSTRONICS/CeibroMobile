package com.zstronics.ceibro.data.repos.projects.group


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse

@Keep
data class CreateProjectGroupResponse(
    @SerializedName("result")
    val result: ProjectGroup
) : BaseResponse()