package com.zstronics.ceibro.data.database.models.tasks

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.database.TableNames

@Entity(tableName = TableNames.Tasks)
data class ProjectTask(
    @PrimaryKey(autoGenerate = true)
    val taskId: Int,
    @SerializedName("_id") val _id: String,
    @SerializedName("access") val access: List<String>,
    @SerializedName("admins") val admins: List<String>,
    @SerializedName("advanceOptions") val advanceOptions: AdvanceOptions,
    @SerializedName("advanceOptionsEnabled") val advanceOptionsEnabled: Boolean,
    @SerializedName("assignedTo") val assignedTo: List<String>,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("creator") val creator: String,
    @SerializedName("dueDate") val dueDate: String,
    @SerializedName("isMultiTask") val isMultiTask: Boolean,
    @SerializedName("project") val project: String,
    @SerializedName("state") val state: String,
    @SerializedName("subTaskStatusCount") val subTaskStatusCount: List<ProjectSubTask>,
    @SerializedName("title") val title: String,
    @SerializedName("unSeenSubTaskCommentCount") val unSeenSubTaskCommentCount: Int,
    @SerializedName("updatedAt") val updatedAt: String,
    @SerializedName("__v") val v: Int
)

@Entity(tableName = TableNames.SubTasks)
data class ProjectSubTask(
    @PrimaryKey
    val id: Int,
    @SerializedName("state") val state: String,
    @SerializedName("count") val count: String,
    @SerializedName("subTasks") val subTasks: List<String>,
)
