package com.zstronics.ceibro.data.repos.task.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class UpdateDraftTaskRequestNoAdvanceOptions(
    @SerializedName("admins") val admins: List<String>,
    @SerializedName("assignedTo") val assignedTo: List<String>,
    @SerializedName("dueDate") val dueDate: String,
    @SerializedName("isMultiTask") val isMultiTask: Boolean,
    @SerializedName("project") val project: String,
    @SerializedName("state") val state: String,
    @SerializedName("description") val description: String,
    @SerializedName("title") val title: String
)