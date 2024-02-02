package com.zstronics.ceibro.data.database.models.inbox

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.Entity
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import com.zstronics.ceibro.data.database.TableNamesV2
import com.zstronics.ceibro.data.database.models.tasks.TaskMemberDetail
import kotlinx.parcelize.Parcelize

@Entity(tableName = TableNamesV2.Inbox, primaryKeys = ["taskId"])
@Parcelize
@Keep
data class CeibroInboxV2(
    @SerializedName("_id")
    val _id: String,
    @SerializedName("actionBy")
    var actionBy: TaskMemberDetail,
    @SerializedName("actionDataTask")
    val actionDataTask: ActionDataTask,
    @SerializedName("actionDescription")
    var actionDescription: String,
    @SerializedName("actionFiles")
    var actionFiles: MutableList<ActionFilesData>,
    @SerializedName("actionTitle")
    val actionTitle: String,
    @SerializedName("actionType")
    var actionType: String,
    @SerializedName("commentId")
    val commentId: String?,
    @SerializedName("createdAt")
    var createdAt: String,
    @SerializedName("eventId")
    val eventId: String?,
    @SerializedName("isSeen")
    var isSeen: Boolean,
    @SerializedName("sentTo")
    val sentTo: TaskMemberDetail,
    @SerializedName("taskId")
    val taskId: String,
    @SerializedName("taskState")
    var taskState: String,
    @SerializedName("unSeenNotifCount")
    var unSeenNotifCount: Int,
    @SerializedName("updatedAt")
    val updatedAt: String
) : BaseResponse(), Parcelable


@Parcelize
@Keep
data class ActionDataTask(
    @SerializedName("_id")
    val _id: String,
    @SerializedName("dueDate")
    val dueDate: String,
    @SerializedName("project")
    val project: ActionProjectData?,
    @SerializedName("state")
    val state: String,
    @SerializedName("taskUID")
    val taskUID: String
) : Parcelable


@Parcelize
@Keep
data class ActionProjectData(
    @SerializedName("title")
    val title: String
) : Parcelable


@Parcelize
@Keep
data class ActionFilesData(
    @SerializedName("fileUrl")
    val fileUrl: String
) : Parcelable