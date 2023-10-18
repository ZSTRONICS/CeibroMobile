package com.zstronics.ceibro.data.repos.task.models.v2

import android.location.Location
import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import com.zstronics.ceibro.data.database.models.tasks.AssignedToState
import com.zstronics.ceibro.data.database.models.tasks.CommentData
import com.zstronics.ceibro.data.database.models.tasks.EventData
import com.zstronics.ceibro.data.database.models.tasks.Topic
import com.zstronics.ceibro.data.repos.chat.room.Initiator
import com.zstronics.ceibro.data.repos.chat.room.Project
import kotlinx.parcelize.Parcelize

@Parcelize
@Keep
data class AllTasksResponse(
    @SerializedName("data")
    val data: Data
) : BaseResponse(), Parcelable

@Parcelize
@Keep
data class Data(
    @SerializedName("allTasks")
    val allTasks: List<AllTask>,
    @SerializedName("allEvents")
    val allEvents: List<AllEvent>,
    @SerializedName("latestUpdatedAt")
    val latestUpdatedAt: String
) : Parcelable

@Parcelize
@Keep
data class AllTask(
    @SerializedName("_id")
    val id: String,
    @SerializedName("dueDate")
    val dueDate: String,
    @SerializedName("doneImageRequired")
    val doneImageRequired: Boolean,
    @SerializedName("doneCommentsRequired")
    val doneCommentsRequired: Boolean,
    @SerializedName("description")
    val description: String,
    @SerializedName("project")
    val project: Project,
    @SerializedName("locations")
    val locations: List<Location>,
    @SerializedName("topic")
    val topic: Topic,
    @SerializedName("assignedToState")
    val assignedToState: List<AssignedToState>,
    @SerializedName("taskUID")
    val taskUID: String,
    @SerializedName("access")
    val access: List<String>,
    @SerializedName("invitedNumbers")
    val invitedNumbers: List<String>,
    @SerializedName("seenBy")
    val seenBy: List<String>,
    @SerializedName("hiddenBy")
    val hiddenBy: List<String>,
    @SerializedName("isCanceled")
    val isCanceled: Boolean,
    @SerializedName("creatorState")
    val creatorState: String,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("updatedAt")
    val updatedAt: String,
    @SerializedName("__v")
    val v: Int,
    @SerializedName("files")
    val files: List<String>,
    @SerializedName("rootState")
    val rootState: String,
    @SerializedName("userSubState")
    val userSubState: String,
    @SerializedName("isCreator")
    val isCreator: Boolean,
    @SerializedName("isAssignedToMe")
    val isAssignedToMe: Boolean,
    @SerializedName("isHiddenByMe")
    val isHiddenByMe: Boolean,
    @SerializedName("isSeenByMe")
    val isSeenByMe: Boolean,
    @SerializedName("fromMeState")
    val fromMeState: String,
    @SerializedName("toMeState")
    val toMeState: String,
    @SerializedName("hiddenState")
    val hiddenState: String
) : Parcelable


@Parcelize
@Keep
data class AllEvent(
    @SerializedName("_id")
    val id: String,
    @SerializedName("taskId")
    val taskId: String,
    @SerializedName("eventType")
    val eventType: String,
    @SerializedName("initiator")
    val initiator: Initiator,
    @SerializedName("invitedMembers")
    val invitedMembers: List<String>,
    @SerializedName("eventData")
    val eventData: EventData,
    @SerializedName("commentData")
    val commentData: CommentData,
    @SerializedName("eventSeenBy")
    val eventSeenBy: List<String>,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("updatedAt")
    val updatedAt: String
) : Parcelable


