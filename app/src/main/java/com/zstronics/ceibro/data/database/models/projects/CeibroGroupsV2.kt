package com.zstronics.ceibro.data.database.models.projects

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.Entity
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import com.zstronics.ceibro.data.database.TableNamesV2
import com.zstronics.ceibro.data.database.models.tasks.TaskMemberDetail
import com.zstronics.ceibro.data.repos.projects.drawing.DrawingV2
import kotlinx.parcelize.Parcelize


@Keep
@Parcelize
@Entity(tableName = TableNamesV2.Groups, primaryKeys = ["_id"])
data class CeibroGroupsV2(
    @SerializedName("_id")
    var _id: String,
    @SerializedName("createdAt")
    var createdAt: String,
    @SerializedName("creator")
    var creator: TaskMemberDetail,
    @SerializedName("deleted")
    val deleted: Boolean,
    @SerializedName("drawings")
    var drawings: List<DrawingV2> = listOf(),
//    @SerializedName("sharedWith")
//    var sharedWith: List<String>? = listOf(),
    @SerializedName("groupName")
    var groupName: String,
    @SerializedName("projectId")
    var projectId: String,
    @SerializedName("updatedAt")
    var updatedAt: String,
    @SerializedName("isFavoriteByMe")
    var isFavoriteByMe: Boolean,
    @SerializedName("hasAccess")
    var hasAccess: Boolean,
    @SerializedName("isHiddenByMe")
    var isHiddenByMe: Boolean
//    @SerializedName("isPublicGroup")
//    var isPublicGroup: Boolean

) : BaseResponse(), Parcelable
