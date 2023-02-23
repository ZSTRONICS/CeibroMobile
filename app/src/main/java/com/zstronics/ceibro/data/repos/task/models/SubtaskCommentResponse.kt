package com.zstronics.ceibro.data.repos.task.models


import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import com.zstronics.ceibro.data.database.models.tasks.TaskMember

data class SubtaskCommentResponse(
    @SerializedName("result")
    val result: Result
) : BaseResponse() {
    data class Result(
        @SerializedName("access")
        val access: List<String>,
        @SerializedName("createdAt")
        val createdAt: String,
        @SerializedName("files")
        val files: List<Any>,
        @SerializedName("_id")
        val id: String,
        @SerializedName("isFileAttached")
        val isFileAttached: Boolean,
        @SerializedName("message")
        val message: String,
        @SerializedName("seenBy")
        val seenBy: List<String>,
        @SerializedName("sender")
        val sender: TaskMember,
        @SerializedName("subTaskId")
        val subTaskId: String,
        @SerializedName("taskId")
        val taskId: String,
        @SerializedName("updatedAt")
        val updatedAt: String,
        @SerializedName("userState")
        val userState: String
    ) : BaseResponse()
}