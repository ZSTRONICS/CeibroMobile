package com.zstronics.ceibro.data.repos.task.models


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse

@Keep
data class SubTaskByTaskResponse(
    @SerializedName("results")
    val results: SubTaskByTaskResults
) : BaseResponse()