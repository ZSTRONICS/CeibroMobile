package com.zstronics.ceibro.data.repos.task.models.v2


import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.database.TableNamesV2
import com.zstronics.ceibro.data.database.models.projects.CeibroProjectV2
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.AllCeibroConnections
import com.zstronics.ceibro.data.repos.task.models.TopicsResponse
import ee.zstronics.ceibro.camera.AttachmentTypes

// We will use this request as a DB table as well.
@Entity(tableName = TableNamesV2.DraftNewTask)
@Keep
data class NewTaskV2Entity(
    @SerializedName("topic") val topic: String,
    @SerializedName("project") val project: String,
    @SerializedName("assignedToState") val assignedToState: List<AssignedToStateNewEntity>,
    @SerializedName("dueDate") val dueDate: String,
    @SerializedName("creator") val creator: String,
    @SerializedName("description") val description: String,
    @SerializedName("doneImageRequired") val doneImageRequired: Boolean,
    @SerializedName("doneCommentsRequired") val doneCommentsRequired: Boolean,
    @SerializedName("invitedNumbers") val invitedNumbers: List<String>,
    @SerializedName("hasPendingFilesToUpload") val hasPendingFilesToUpload: Boolean,

    /// RoomDB specific rows
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "task_id") @Transient val taskId: Int = 0,
    @ColumnInfo(name = "files_data") @Transient var filesData: List<LocalFilesToStore>? = null,
    @ColumnInfo(name = "isNewTaskCreationFailed") @Transient var isNewTaskCreationFailed: Boolean = false,
    @ColumnInfo(name = "isDraftTaskCreationFailed") @Transient var isDraftTaskCreationFailed: Boolean = false,
    @ColumnInfo(name = "taskCreationFailedError") @Transient var taskCreationFailedError: String = ""
) {
    @Keep
    data class AssignedToStateNewEntity(
        @SerializedName("phoneNumber") val phoneNumber: String,
        @SerializedName("userId") val userId: String,
        @SerializedName("state") val state: String = "new",
    )
}

@Keep
data class NewTaskToSave(
    @SerializedName("topic") val topic: TopicsResponse.TopicData?,
    @SerializedName("project") val project: CeibroProjectV2?,
    @SerializedName("selectedContacts") val selectedContacts: List<AllCeibroConnections.CeibroConnection>?,
    @SerializedName("dueDate") val dueDate: String?,
    @SerializedName("selfAssigned") val selfAssigned: Boolean?
)

data class LocalFilesToStore(
    var fileUri: String,
    var comment: String = "",
    val fileName: String = "",
    val fileSizeReadAble: String = "",
    val editingApplied: Boolean = false,
    val attachmentType: AttachmentTypes,
)