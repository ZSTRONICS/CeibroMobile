package com.zstronics.ceibro.data.repos.task.models.v2


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ForwardTaskV2Request(
    @SerializedName("assignedToState")
    val assignedToState: List<AssignedToStateRequest>,
    @SerializedName("invitedNumbers")
    val invitedNumbers: List<String>,
    @SerializedName("comment")
    val comment: String
) {
    @Keep
    data class AssignedToStateRequest(
        @SerializedName("phoneNumber") val phoneNumber: String,
        @SerializedName("userId") val userId: String,
        @SerializedName("state") val state: String = "new",
    )
}