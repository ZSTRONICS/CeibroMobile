package com.zstronics.ceibro.data.repos.task.models


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import com.zstronics.ceibro.data.database.models.tasks.CeibroTask

@Keep
data class NewTaskResponse(
    @SerializedName("newTask") val newTask: CeibroTask?
) : BaseResponse()