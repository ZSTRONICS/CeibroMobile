package com.zstronics.ceibro.data.repos.task.models.v2

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import com.zstronics.ceibro.data.database.models.tasks.Events

@Keep
data class SyncTasksResponse(
    @SerializedName("events")
    var events: List<Events>? = null
) : BaseResponse()

