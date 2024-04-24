package com.zstronics.ceibro.data.repos.dashboard.connections.v2

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.Entity
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import com.zstronics.ceibro.data.database.TableNamesV2
import com.zstronics.ceibro.data.database.models.tasks.AssignedToState
import com.zstronics.ceibro.data.database.models.tasks.TaskMemberDetail
import kotlinx.parcelize.Parcelize

@Entity(tableName = TableNamesV2.ConnectionGroup, primaryKeys = ["_id"])
@Parcelize
@Keep
data class CeibroConnectionGroupV2(
    @SerializedName("_id")
    val _id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("creator")
    val creator: TaskMemberDetail,
    @SerializedName("contacts")
    val contacts: List<TaskMemberDetail>,
    @SerializedName("confirmer")
    val confirmer: List<TaskMemberDetail>,
    @SerializedName("viewer")
    val viewer: List<TaskMemberDetail>,
    @SerializedName("assignedToState")
    val assignedToState: List<AssignedToState>,
    @SerializedName("groupAdmins")
    val groupAdmins: List<TaskMemberDetail>,
    @SerializedName("sharedWith")
    val sharedWith: List<TaskMemberDetail>,
    @SerializedName("access")
    val access: List<String>,
    @SerializedName("isPublic")
    val isPublic: Boolean,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("updatedAt")
    val updatedAt: String
) : BaseResponse(), Parcelable


@Parcelize
@Keep
data class GroupContact(
    @SerializedName("_id")
    val _id: String,
    @SerializedName("contactFirstName")
    val contactFirstName: String,
    @SerializedName("contactFullName")
    val contactFullName: String,
    @SerializedName("contactSurName")
    val contactSurName: String,
    @SerializedName("isCeiborUser")
    val isCeibroUser: Boolean,
    @SerializedName("phoneNumber")
    val phoneNumber: String,
    @SerializedName("userCeibroData")
    val userCeibroData: AllCeibroConnections.CeibroConnection.UserCeibroData?
) : Parcelable