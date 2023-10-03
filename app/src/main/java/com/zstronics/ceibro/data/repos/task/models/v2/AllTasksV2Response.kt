package com.zstronics.ceibro.data.repos.task.models.v2


import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import com.zstronics.ceibro.data.repos.task.models.TaskV2Response

data class AllTasksV2Response(
    @SerializedName("allTasks")
    val allTasks: AllTasksData
) : BaseResponse() {
    data class AllTasksData(
        @SerializedName("fromMe")
        val fromMe: TaskV2Response.AllTasks,
        @SerializedName("hidden")
        val hidden: TaskV2Response.AllTasks,
        @SerializedName("toMe")
        val toMe: TaskV2Response.AllTasks,
        @SerializedName("latestUpdatedAt")
        val latestUpdatedAt: String,
    )
}