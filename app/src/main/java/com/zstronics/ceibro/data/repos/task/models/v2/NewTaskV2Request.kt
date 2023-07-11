package com.zstronics.ceibro.data.repos.task.models.v2


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class NewTaskV2Request(
    @SerializedName("topic") val topic: String,
    @SerializedName("project") val project: String,
    @SerializedName("assignedToState") val assignedToState: List<AssignedToStateNewRequest>,
    @SerializedName("dueDate") val dueDate: String,
    @SerializedName("creator") val creator: String,
    @SerializedName("description") val description: String,
    @SerializedName("doneImageRequired") val doneImageRequired: Boolean,
    @SerializedName("doneCommentsRequired") val doneCommentsRequired: Boolean,
    @SerializedName("invitedNumbers") val invitedNumbers: List<String>
) {
    @Keep
    data class AssignedToStateNewRequest(
        @SerializedName("phoneNumber") val phoneNumber: String,
        @SerializedName("userId") val userId: String,
        @SerializedName("state") val state: String = "new",
    )
}
