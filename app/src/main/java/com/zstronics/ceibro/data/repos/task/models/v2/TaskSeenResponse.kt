package com.zstronics.ceibro.data.repos.task.models.v2


import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import com.zstronics.ceibro.data.database.models.tasks.AssignedToState
import com.zstronics.ceibro.data.database.models.tasks.CeibroDrawingPins
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
        @SerializedName("taskId")
        val taskId: String,
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
        val eventInitiator: String,
        @SerializedName("updatedAt")
        val updatedAt: String,
        @SerializedName("taskUpdatedAt")
        val taskUpdatedAt: String,
        @SerializedName("newTaskData")
        val newTaskData: EventV2Response.Data.TaskStatesData,
        @SerializedName("oldTaskData")
        val oldTaskData: EventV2Response.Data.TaskStatesData,
        @SerializedName("pinData")
        val pinData: CeibroDrawingPins?
    ) : Parcelable

}