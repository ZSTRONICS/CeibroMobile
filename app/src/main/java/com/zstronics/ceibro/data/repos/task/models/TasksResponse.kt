package com.zstronics.ceibro.data.repos.task.models


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import com.zstronics.ceibro.data.database.models.tasks.ProjectTask

@Keep
data class TasksResponse(
    @SerializedName("allTasks") val allTasks: List<ProjectTask>
) : BaseResponse()