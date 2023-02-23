package com.zstronics.ceibro.data.repos.task.models


import com.google.gson.annotations.SerializedName

data class SubtaskCommentRequest(
    @SerializedName("access")
    val access: List<String>?,
    @SerializedName("isFileAttached")
    val isFileAttached: Boolean?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("seenBy")
    val seenBy: List<String>?,
    @SerializedName("sender")
    val sender: String?,
    @SerializedName("subTaskId")
    val subTaskId: String?,
    @SerializedName("taskId")
    val taskId: String?,
    @SerializedName("userState")
    val userState: String?
)