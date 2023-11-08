package com.zstronics.ceibro.data.repos.task.models.v2


import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import com.zstronics.ceibro.data.database.models.tasks.AssignedToState
import com.zstronics.ceibro.data.database.models.tasks.CommentData
import com.zstronics.ceibro.data.database.models.tasks.EventData
import com.zstronics.ceibro.data.database.models.tasks.ForwardData
import com.zstronics.ceibro.data.database.models.tasks.InvitedNumbers
import com.zstronics.ceibro.data.database.models.tasks.TaskMemberDetail
import kotlinx.parcelize.Parcelize

@Parcelize
@Keep
data class EventV2Response(
    @SerializedName("data")
    val `data`: Data
) : BaseResponse(), Parcelable {

    @Parcelize
    @Keep
    data class Data(
        @SerializedName("commentData")
        val commentData: CommentData?,
        @SerializedName("createdAt")
        val createdAt: String,
        @SerializedName("eventData")
        val eventData: List<EventData>?,
        @SerializedName("invitedMembers")
        val invitedMembers: List<EventData>?,
        @SerializedName("eventType")
        val eventType: String,
        @SerializedName("_id")
        val id: String,
        @SerializedName("initiator")
        val initiator: TaskMemberDetail,
        @SerializedName("taskId")
        val taskId: String,
        @SerializedName("taskData")
        val taskData: TaskData,
        @SerializedName("newTaskData")
        val newTaskData: TaskStatesData,
        @SerializedName("oldTaskData")
        val oldTaskData: TaskStatesData,
        @SerializedName("updatedAt")
        val updatedAt: String,
        @SerializedName("taskUpdatedAt")
        val taskUpdatedAt: String,
        @SerializedName("eventNumber")
        val eventNumber: Int
    ) : Parcelable {

        @Parcelize
        @Keep
        data class TaskData(
            @SerializedName("creatorState")
            val creatorState: String,
            @SerializedName("hiddenBy")
            val hiddenBy: List<String>,
            @SerializedName("seenBy")
            val seenBy: List<String>,
            @SerializedName("creator")
            val creator: String,
            @SerializedName("invitedNumbers")
            val invitedNumbers: List<InvitedNumbers>,
            @SerializedName("assignedToState")
            val assignedToState: List<AssignedToState>
        ) : Parcelable


        @Parcelize
        @Keep
        data class TaskStatesData(
            @SerializedName("creatorState")
            val creatorState: String,
            @SerializedName("isAssignedToMe")
            val isAssignedToMe: Boolean,
            @SerializedName("isCreator")
            val isCreator: Boolean,
            @SerializedName("isHiddenByMe")
            val isHiddenByMe: Boolean,
            @SerializedName("isSeenByMe")
            val isSeenByMe: Boolean,
            @SerializedName("userSubState")
            val userSubState: String,
            @SerializedName("fromMeState")
            val fromMeState: String,
            @SerializedName("toMeState")
            val toMeState: String,
            @SerializedName("hiddenState")
            val hiddenState: String
        ) : Parcelable
    }
}