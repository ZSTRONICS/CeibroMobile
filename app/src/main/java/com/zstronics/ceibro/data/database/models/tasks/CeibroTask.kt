package com.zstronics.ceibro.data.database.models.tasks

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.database.TableNames
import kotlinx.parcelize.Parcelize

@Entity(tableName = TableNames.Tasks)
@Parcelize
data class CeibroTask(
    @PrimaryKey(autoGenerate = true)
    val taskId: Int,
    @SerializedName("_id") val _id: String,
    @SerializedName("access") val access: List<String>,
    @SerializedName("admins") val admins: List<TaskMember>,
    @SerializedName("advanceOptions") val advanceOptions: AdvanceOptions,
    @SerializedName("advanceOptionsEnabled") val advanceOptionsEnabled: Boolean,
    @SerializedName("assignedTo") val assignedTo: List<TaskMember>,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("creator") val creator: TaskMember?,
    @SerializedName("dueDate") val dueDate: String,
    @SerializedName("isMultiTask") val isMultiTask: Boolean,
    @SerializedName("project") val project: TaskProject,
    @SerializedName("state") val state: String,
    @SerializedName("subTaskStatusCount") val subTaskStatusCount: List<ProjectSubTaskStatus>,
    @SerializedName("title") val title: String,
    @SerializedName("unSeenSubTaskCommentCount") val unSeenSubTaskCommentCount: Int,
    @SerializedName("updatedAt") val updatedAt: String,
    @SerializedName("__v") val v: Int,
    @SerializedName("totalSubTaskCount") val totalSubTaskCount: Int
) : Parcelable

@Entity(tableName = TableNames.SubTasks)
@Parcelize
data class ProjectSubTaskStatus(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    @SerializedName("state") val state: String,
    @SerializedName("count") val count: String,
    @SerializedName("subTasks") val subTasks: List<String>,
) : Parcelable

@Entity(tableName = TableNames.TaskMember)
@Parcelize
data class TaskMember(
    @PrimaryKey(autoGenerate = true)
    val TaskMemberId: Int,
    @SerializedName("firstName") val firstName: String,
    @SerializedName("surName") val surName: String,
    @SerializedName("profilePic") val profilePic: String,
    @SerializedName("_id") val id: String,
) : Parcelable

@Entity(tableName = TableNames.TaskMember)
@Parcelize
data class TaskProject(
    @PrimaryKey(autoGenerate = true)
    val TaskProjectId: Int,
    @SerializedName("title") val title: String,
    @SerializedName("_id") val id: String,
) : Parcelable
