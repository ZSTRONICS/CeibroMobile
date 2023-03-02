package com.zstronics.ceibro.data.repos.task.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import com.zstronics.ceibro.data.database.models.subtask.SubTaskComments

@Keep
data class AllCommentsResponse(
    @SerializedName("result") val result: ArrayList<SubTaskComments>
) : BaseResponse()