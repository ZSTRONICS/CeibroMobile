package com.zstronics.ceibro.data.repos.task.models


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep

@Keep
data class UpdateDraftSubtaskRequest(
    @SerializedName("assignedTo") val assignedTo: List<AssignedTo>,
    @SerializedName("description") val description: String,
    @SerializedName("dueDate") val dueDate: String,
    @SerializedName("state") val state: List<State>?,
    @SerializedName("title") val title: String
) {
    @Keep
    data class AssignedTo(
        @SerializedName("addedBy") val addedBy: String?,
        @SerializedName("members") val members: List<String>?
    )

    @Keep
    data class Viewer(
        @SerializedName("addedBy") val addedBy: String?,
        @SerializedName("members") val members: List<String>?
    )

    @Keep
    data class State(
        @SerializedName("userId") val userId: String?,
        @SerializedName("userState") val userState: String?
    )
}