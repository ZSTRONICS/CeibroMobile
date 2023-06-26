package com.zstronics.ceibro.data.repos.task.models.v2


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ForwardTaskV2Request(
    @SerializedName("assignedToState")
    val assignedToState: List<AssignedToState>,
    @SerializedName("invitedNumbers")
    val invitedNumbers: List<String>
) {
    @Keep
    data class AssignedToState(
        @SerializedName("phoneNumber") val phoneNumber: String,
        @SerializedName("userId") val userId: String,
        @SerializedName("state") val state: String = "new",
    )
}