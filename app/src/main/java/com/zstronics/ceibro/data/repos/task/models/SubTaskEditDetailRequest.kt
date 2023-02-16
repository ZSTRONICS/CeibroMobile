package com.zstronics.ceibro.data.repos.task.models


import com.google.gson.annotations.SerializedName

data class SubTaskEditDetailRequest(
    @SerializedName("memberId")
    val memberId: String,
    @SerializedName("subTaskId")
    val subTaskId: String,
    @SerializedName("taskId")
    val taskId: String
)