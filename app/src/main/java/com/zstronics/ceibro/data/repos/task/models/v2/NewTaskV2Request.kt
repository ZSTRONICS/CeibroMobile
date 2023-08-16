package com.zstronics.ceibro.data.repos.task.models.v2


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.AllCeibroConnections
import com.zstronics.ceibro.data.repos.projects.projectsmain.AllProjectsResponseV2
import com.zstronics.ceibro.data.repos.task.models.TopicsResponse

@Keep
data class NewTaskV2Request(
    @SerializedName("topic") val topic: String,
    @SerializedName("project") val project: String,
    @SerializedName("assignedToState") val assignedToState: List<AssignedToStateNewRequest>,
    @SerializedName("dueDate") val dueDate: String,
    @SerializedName("creator") val creator: String,
    @SerializedName("description") val description: String,
    @SerializedName("doneImageRequired") val doneImageRequired: Boolean,
    @SerializedName("doneCommentsRequired") val doneCommentsRequired: Boolean,
    @SerializedName("invitedNumbers") val invitedNumbers: List<String>,
    @SerializedName("hasPendingFilesToUpload") val hasPendingFilesToUpload: Boolean
) {
    @Keep
    data class AssignedToStateNewRequest(
        @SerializedName("phoneNumber") val phoneNumber: String,
        @SerializedName("userId") val userId: String,
        @SerializedName("state") val state: String = "new",
    )
}

@Keep
data class NewTaskToSave(
    @SerializedName("topic") val topic: TopicsResponse.TopicData?,
    @SerializedName("project") val project: AllProjectsResponseV2.ProjectsV2?,
    @SerializedName("selectedContacts") val selectedContacts: List<AllCeibroConnections.CeibroConnection>?,
    @SerializedName("dueDate") val dueDate: String?,
    @SerializedName("selfAssigned") val selfAssigned: Boolean?
)
