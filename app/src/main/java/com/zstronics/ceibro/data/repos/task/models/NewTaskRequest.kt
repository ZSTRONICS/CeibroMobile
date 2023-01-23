package com.zstronics.ceibro.data.repos.task.models


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class NewTaskRequest(
    @SerializedName("admins") val admins: List<String>,
    @SerializedName("advanceOptions") val advanceOptions: AdvanceOptions,
    @SerializedName("assignedTo") val assignedTo: List<String>,
    @SerializedName("creator") val creator: String,
    @SerializedName("dueDate") val dueDate: String,
    @SerializedName("isMultiTask") val isMultiTask: Boolean,
    @SerializedName("project") val project: String,
    @SerializedName("state") val state: String,
    @SerializedName("description") val description: String,
    @SerializedName("title") val title: String
) {
    @Keep
    data class AdvanceOptions(
        @SerializedName("categories") val categories: List<String>,
        @SerializedName("confirmNeeded") val confirmNeeded: List<String>,
        @SerializedName("isAdditionalWork") val isAdditionalWork: Boolean,
        @SerializedName("location") val location: String,
        @SerializedName("manPower") val manPower: Int,
        @SerializedName("priority") val priority: String,
        @SerializedName("startDate") val startDate: String,
        @SerializedName("timeLogging") val timeLogging: Boolean,
        @SerializedName("viewer") val viewer: List<String>
    )
}

@Keep
data class NewTaskRequestNoAdvanceOptions(
    @SerializedName("admins") val admins: List<String>,
    @SerializedName("assignedTo") val assignedTo: List<String>,
    @SerializedName("creator") val creator: String,
    @SerializedName("dueDate") val dueDate: String,
    @SerializedName("isMultiTask") val isMultiTask: Boolean,
    @SerializedName("project") val project: String,
    @SerializedName("state") val state: String,
    @SerializedName("description") val description: String,
    @SerializedName("title") val title: String
)