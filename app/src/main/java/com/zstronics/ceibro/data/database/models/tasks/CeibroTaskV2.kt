package com.zstronics.ceibro.data.database.models.tasks


import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
@Keep
data class CeibroTaskV2(
    @SerializedName("access")
    val access: List<String>,
    @SerializedName("assignedToState")
    val assignedToState: List<AssignedToState>,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("creator")
    val creator: TaskMemberDetail,
    @SerializedName("creatorState")
    val creatorState: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("doneCommentsRequired")
    val doneCommentsRequired: Boolean,
    @SerializedName("doneImageRequired")
    val doneImageRequired: Boolean,
    @SerializedName("dueDate")
    val dueDate: String,
    @SerializedName("_id")
    val id: String,
    @SerializedName("invitedNumbers")
    val invitedNumbers: List<InvitedNumbers>,
    @SerializedName("locations")
    val locations: List<String>,
    @SerializedName("project")
    val project: ProjectOfTask?,
    @SerializedName("recentComments")
    val recentComments: List<String>,
    @SerializedName("rejectionComments")
    val rejectionComments: List<String>,
    @SerializedName("taskUID")
    val taskUID: String,
    @SerializedName("topic")
    val topic: Topic?,
    @SerializedName("updatedAt")
    val updatedAt: String,
    @SerializedName("__v")
    val v: Int,
    @SerializedName("files")
    var files: List<Files>,
    @SerializedName("events")
    val events: List<Events>
) : Parcelable

@Parcelize
@Keep
data class Topic(
    @SerializedName("_id")
    val id: String,
    @SerializedName("topic")
    val topic: String
) : Parcelable

@Parcelize
@Keep
data class ProjectOfTask(
    @SerializedName("_id")
    val id: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("location")
    val location: String
) : Parcelable


@Parcelize
@Keep
data class TaskMemberDetail (
    @SerializedName("firstName") val firstName: String,
    @SerializedName("surName") val surName: String,
    @SerializedName("profilePic") val profilePic: String?,
    @SerializedName("_id") val id: String,
) : Parcelable


@Parcelize
@Keep
data class AssignedToState(
    @SerializedName("firstName")
    val firstName: String,
    @SerializedName("_id")
    val id: String,
    @SerializedName("phoneNumber")
    val phoneNumber: String,
    @SerializedName("profilePic")
    val profilePic: String,
    @SerializedName("state")
    val state: String,
    @SerializedName("surName")
    val surName: String,
    @SerializedName("userId")
    val userId: String
) : Parcelable


@Parcelize
@Keep
data class Files(
    @SerializedName("access")
    val access: List<String>,
    @SerializedName("comment")
    val comment: String,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("fileName")
    val fileName: String,
    @SerializedName("fileTag")
    val fileTag: String,
    @SerializedName("fileType")
    val fileType: String,
    @SerializedName("fileUrl")
    val fileUrl: String,
    @SerializedName("hasComment")
    val hasComment: Boolean,
    @SerializedName("_id")
    val id: String,
    @SerializedName("moduleId")
    val moduleId: String,
    @SerializedName("moduleType")
    val moduleType: String,
    @SerializedName("updatedAt")
    val updatedAt: String,
    @SerializedName("uploadStatus")
    val uploadStatus: String,
    @SerializedName("uploadedBy")
    val uploadedBy: TaskMemberDetail,
    @SerializedName("__v")
    val v: Int,
    @SerializedName("version")
    val version: Int
) : Parcelable

@Parcelize
@Keep
data class Events(
    @SerializedName("comment")
    val comment: String,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("eventData")
    val eventData: List<EventData>?,
    @SerializedName("commentData")
    val commentData: CommentData?,
    @SerializedName("eventType")
    val eventType: String,
    @SerializedName("_id")
    val id: String,
    @SerializedName("initiator")
    val initiator: TaskMemberDetail,
    @SerializedName("taskId")
    val taskId: String,
    @SerializedName("updatedAt")
    val updatedAt: String,
    @SerializedName("__v")
    val v: Int
) : Parcelable


@Parcelize
@Keep
data class EventData(
    @SerializedName("firstName")
    val firstName: String,
    @SerializedName("_id")
    val id: String?,
    @SerializedName("phoneNumber")
    val phoneNumber: String?,
    @SerializedName("profilePic")
    val profilePic: String?,
    @SerializedName("surName")
    val surName: String
) : Parcelable


@Parcelize
@Keep
data class CommentData(
    @SerializedName("files")
    val files: List<Files>,
    @SerializedName("_id")
    val id: String,
    @SerializedName("isFileAttached")
    val isFileAttached: Boolean,
    @SerializedName("message")
    val message: String
) : Parcelable

@Parcelize
@Keep
data class InvitedNumbers(
    @SerializedName("firstName")
    val firstName: String,
    @SerializedName("phoneNumber")
    val phoneNumber: String,
    @SerializedName("surName")
    val surName: String
) : Parcelable