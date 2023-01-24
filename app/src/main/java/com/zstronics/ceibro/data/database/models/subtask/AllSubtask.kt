package com.zstronics.ceibro.data.database.models.subtask


import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.database.TableNames
import com.zstronics.ceibro.data.database.models.tasks.TaskMember
import kotlinx.parcelize.Parcelize

@Entity(tableName = TableNames.SubTasks)
@Parcelize
data class AllSubtask(
    @PrimaryKey(autoGenerate = true)
    val subTaskId: Int,
    @SerializedName("_id") val id: String,
    @SerializedName("access") val access: List<String>,
    @SerializedName("advanceOptions") val advanceOptions: SubTaskAdvanceOptions,
    @SerializedName("assignedTo") val assignedTo: List<AssignedTo>,
    @SerializedName("comments") val comments: List<String>,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("creator") val creator: TaskMember,
    @SerializedName("description") val description: String,
    @SerializedName("doneCommentsRequired") val doneCommentsRequired: Boolean,
    @SerializedName("advanceOptionsEnabled") val advanceOptionsEnabled: Boolean,
    @SerializedName("doneImageRequired") val doneImageRequired: Boolean,
    @SerializedName("dueDate") val dueDate: String,
    @SerializedName("files") val files: List<String>,
    @SerializedName("isMultiTaskSubTask") val isMultiTaskSubTask: Boolean,
    @SerializedName("state") val state: String,
    @SerializedName("subTaskFixedForUser") val subTaskFixedForUser: List<String>,
    @SerializedName("taskId") val taskId: String,
    @SerializedName("title") val title: String,
    @SerializedName("updatedAt") val updatedAt: String,
    @SerializedName("viewer") val viewer: List<Viewer>
) : Parcelable


@Entity(tableName = TableNames.AssignedTo)
@Parcelize
data class AssignedTo(
    @PrimaryKey(autoGenerate = true)
    val assignedToId: Int,
    @SerializedName("addedBy") val addedBy: TaskMember,
    @SerializedName("_id") val id: String,
    @SerializedName("members") val members: List<TaskMember>
) : Parcelable


@Entity(tableName = TableNames.Viewer)
@Parcelize
data class Viewer(
    @PrimaryKey(autoGenerate = true)
    val viewerId: Int,
    @SerializedName("addedBy") val addedBy: TaskMember,
    @SerializedName("_id") val id: String,
    @SerializedName("members") val members: List<TaskMember>
) : Parcelable