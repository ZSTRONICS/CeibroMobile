package com.zstronics.ceibro.data.repos.task.models.v2

import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import com.zstronics.ceibro.data.database.models.tasks.CeibroTaskV2

data class NewTaskV2Response(
    @SerializedName("newTask")
    val newTask: CeibroTaskV2
) : BaseResponse()