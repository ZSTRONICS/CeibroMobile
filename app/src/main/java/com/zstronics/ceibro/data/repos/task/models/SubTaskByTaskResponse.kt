package com.zstronics.ceibro.data.repos.task.models


import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse

data class SubTaskByTaskResponse(
    @SerializedName("results")
    val results: SubTaskByTaskResults
) : BaseResponse()