package com.zstronics.ceibro.data.repos.task.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class UpdateTaskRequestNoAdvanceOptions(
    @SerializedName("admins") val admins: List<String>,
    @SerializedName("assignedTo") val assignedTo: List<String>,
    @SerializedName("description") val description: String
)