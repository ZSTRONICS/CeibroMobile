package com.zstronics.ceibro.data.database.models.tasks

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.database.TableNames

@Entity(tableName = TableNames.Tasks)
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
    @SerializedName("creator") val creator: TaskMember,
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
)

@Entity(tableName = TableNames.SubTasks)
data class ProjectSubTaskStatus(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    @SerializedName("state") val state: String,
    @SerializedName("count") val count: String,
    @SerializedName("subTasks") val subTasks: List<String>,
)

@Entity(tableName = TableNames.TaskMember)
data class TaskMember(
    @PrimaryKey(autoGenerate = true)
    val TaskMemberId: Int,
    @SerializedName("firstName") val firstName: String,
    @SerializedName("surName") val surName: String,
    @SerializedName("id") val id: String,
)

@Entity(tableName = TableNames.TaskMember)
data class TaskProject(
    @PrimaryKey(autoGenerate = true)
    val TaskProjectId: Int,
    @SerializedName("title") val title: String,
    @SerializedName("id") val id: String,
)
