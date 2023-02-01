package com.zstronics.ceibro.data.repos.task.models


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep

@Keep
data class UpdateSubTaskStatusRequest(
    @SerializedName("comment") val comment: String,
    @SerializedName("state") val state: String,
    @SerializedName("subTaskId") val subTaskId: String,
    @SerializedName("taskId") val taskId: String
)