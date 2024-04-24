package com.zstronics.ceibro.data.repos.dashboard.connections.v2


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

data class NewConnectionGroupRequest(
    @SerializedName("name")
    val name: String,
    @SerializedName("contacts")
    val contacts: List<String>
)

data class CreateGroupRequest(
    @SerializedName("name")
    val name: String,
    @SerializedName("viewer")
    val viewer: List<String>,
    @SerializedName("confirmer")
    val confirmer: List<String>,
    @SerializedName("assignedToState")
    val assignedToState: List<AssignedToStateNewEntity>,
    @SerializedName("groupAdmins")
    val groupAdmins: List<String>,
    @SerializedName("sharedWith")
    val sharedWith: List<String>,
    @SerializedName("isPublic")
    val isPublic: Boolean = false,
    @SerializedName("assosiatedProjects")
    val assosiatedProjects: List<String>,

    )

data class ConnectionGroupUpdateWithoutNameRequest(
    @SerializedName("viewer")
    val viewer: List<String>,
    @SerializedName("confirmer")
    val confirmer: List<String>,
    @SerializedName("assignedToState")
    val assignedToState: List<AssignedToStateNewEntity>,
    @SerializedName("groupAdmins")
    val groupAdmins: List<String>,
    @SerializedName("sharedWith")
    val sharedWith: List<String>,
    @SerializedName("isPublic")
    val isPublic: Boolean = false,
    @SerializedName("assosiatedProjects")
    val assosiatedProjects: List<String>,

    )

@Keep
data class AssignedToStateNewEntity(
    @SerializedName("phoneNumber") val phoneNumber: String,
    @SerializedName("userId") val userId: String,
    @SerializedName("state") val state: String = "new",
)