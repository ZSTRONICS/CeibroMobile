package com.zstronics.ceibro.data.database.models.subtask


import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.database.TableNames
import com.zstronics.ceibro.data.database.models.tasks.TaskMember
import kotlinx.parcelize.Parcelize

@Entity(tableName = TableNames.SubTasks)
@Parcelize
@Keep
data class AllSubtask(
//    @PrimaryKey(autoGenerate = true)
//    val subTaskId: Int,
    @PrimaryKey
    @SerializedName("_id") val id: String,
    @SerializedName("access") val access: List<String>,
    @SerializedName("rejectedBy") val rejectedBy: List<TaskMember>?,
    @SerializedName("advanceOptions") val advanceOptions: SubTaskAdvanceOptions,
    @SerializedName("assignedToMembersOnly") val assignedToMembersOnly: List<TaskMember>?,
    @SerializedName("assignedTo") val assignedTo: List<AssignedTo>,
    @SerializedName("comments") val comments: List<String>?,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("creator") val creator: TaskMember,
    @SerializedName("description") val description: String?,
    @SerializedName("doneCommentsRequired") val doneCommentsRequired: Boolean,
    @SerializedName("advanceOptionsEnabled") val advanceOptionsEnabled: Boolean,
    @SerializedName("doneImageRequired") val doneImageRequired: Boolean,
    @SerializedName("dueDate") val dueDate: String,
    @SerializedName("files") val files: List<String>?,
    @SerializedName("isMultiTaskSubTask") val isMultiTaskSubTask: Boolean,
    @SerializedName("recentComments") val recentComments: List<SubTaskComments>?,
    @SerializedName("rejectionComments") val rejectionComments: List<SubTaskComments>?,
    @SerializedName("state") var state: List<SubTaskStateItem>?,
    @SerializedName("taskId") val taskId: String,
    @SerializedName("taskData") val taskData: TaskDataOfSubTask?,
    @SerializedName("title") val title: String,
    @SerializedName("updatedAt") val updatedAt: String,
    @SerializedName("viewer") val viewer: List<Viewer>
) : Parcelable


@Entity(tableName = TableNames.AssignedTo)
@Parcelize
@Keep
data class AssignedTo(
    @PrimaryKey(autoGenerate = true)
    val assignedToId: Int,
    @SerializedName("addedBy") val addedBy: TaskMember,
    @SerializedName("_id") val id: String,
    @SerializedName("members") val members: List<TaskMember>
) : Parcelable


@Entity(tableName = TableNames.Viewer)
@Parcelize
@Keep
data class Viewer(
    @PrimaryKey(autoGenerate = true)
    val viewerId: Int,
    @SerializedName("addedBy") val addedBy: TaskMember,
    @SerializedName("_id") val id: String,
    @SerializedName("members") val members: List<TaskMember>
) : Parcelable


@Entity(tableName = TableNames.SubTasksState)
@Parcelize
@Keep
data class SubTaskStateItem(
    @PrimaryKey(autoGenerate = true)
    val subTaskStateId: Int,
    @SerializedName("_id") val id: String,
    @SerializedName("userId") val userId: String,
    @SerializedName("userState") var userState: String
) : Parcelable

@Entity(tableName = TableNames.SubTaskComments)
@Parcelize
@Keep
data class SubTaskComments(
    @PrimaryKey(autoGenerate = true)
    val subTaskCommentId: Int,
    @SerializedName("comment")
    val comment: String,
    @SerializedName("creator")
    val creator: TaskMember,
    @SerializedName("_id")
    val id: String,
    @SerializedName("subtaskStateAtComment")
    val subtaskStateAtComment: String
) : Parcelable

@Entity(tableName = TableNames.TaskDataOfSubTask)
@Parcelize
@Keep
data class TaskDataOfSubTask(
    @PrimaryKey(autoGenerate = true)
    val taskDataId: Int,
    @SerializedName("_id") val id: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("admins") val admins: List<String>?,
    @SerializedName("project") val project: SubTaskProject?
) : Parcelable


@Entity(tableName = TableNames.SubTaskProject)
@Parcelize
@Keep
data class SubTaskProject(
    @PrimaryKey(autoGenerate = true)
    val SubTaskProjectId: Int,
    @SerializedName("title") val title: String,
    @SerializedName("_id") val id: String,
) : Parcelable