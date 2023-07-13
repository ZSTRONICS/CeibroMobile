package com.zstronics.ceibro.data.repos.task.models.v2


import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import com.zstronics.ceibro.data.database.models.tasks.AssignedToState
import kotlinx.parcelize.Parcelize

@Parcelize
@Keep
data class TaskSeenResponse(
    @SerializedName("taskSeen")
    val taskSeen: TaskSeen
) : BaseResponse(), Parcelable {

    @Parcelize
    @Keep
    data class TaskSeen(
        @SerializedName("creatorState")
        val creatorState: String,
        @SerializedName("creatorStateChanged")
        val creatorStateChanged: Boolean,
        @SerializedName("_id")
        val id: String,
        @SerializedName("isAssignedToMe")
        val isAssignedToMe: Boolean,
        @SerializedName("isCreator")
        var isCreator: Boolean,
        @SerializedName("seenBy")
        val seenBy: List<String>,
        @SerializedName("state")
        val state: AssignedToState,
        @SerializedName("stateChanged")
        val stateChanged: Boolean,
        @SerializedName("eventInitiator")
        val eventInitiator: String
    ) : Parcelable

}