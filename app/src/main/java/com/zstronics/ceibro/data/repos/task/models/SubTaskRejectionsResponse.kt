package com.zstronics.ceibro.data.repos.task.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse

@Keep
data class SubTaskRejectionsResponse(
    @SerializedName("result") val result: List<SubTaskRejections>
) : BaseResponse()