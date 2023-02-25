package com.zstronics.ceibro.data.repos.task.models


import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import com.zstronics.ceibro.data.database.models.subtask.SubTaskComments

data class SubtaskCommentResponse(
    @SerializedName("result")
    val result: SubTaskComments
) : BaseResponse()