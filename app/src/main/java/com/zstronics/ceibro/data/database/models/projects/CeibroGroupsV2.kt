package com.zstronics.ceibro.data.database.models.projects

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.Entity
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import com.zstronics.ceibro.data.database.TableNamesV2
import com.zstronics.ceibro.data.database.models.tasks.TaskMemberDetail
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
    @SerializedName("groupName")
    var groupName: String,
    @SerializedName("projectId")
    var projectId: String,
    @SerializedName("updatedAt")
    var updatedAt: String,
    @SerializedName("isFavoriteByMe")
    var isFavoriteByMe: Boolean

) : BaseResponse(), Parcelable

@Keep
@Parcelize
data class DrawingV2(
    @SerializedName("_id")
    val _id: String,
    @SerializedName("access")
    val access: List<String>,
    @SerializedName("comment")
    val comment: String,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("fileName")
    val fileName: String,
    @SerializedName("fileSize")
    val fileSize: String,
    @SerializedName("fileTag")
    val fileTag: String,
    @SerializedName("fileType")
    val fileType: String,
    @SerializedName("fileUrl")
    val fileUrl: String,
    @SerializedName("floor")
    val floor: Floor,
    @SerializedName("groupId")
    val groupId: String,
    @SerializedName("hasComment")
    val hasComment: Boolean,
    @SerializedName("moduleType")
    val moduleType: String,
    @SerializedName("projectId")
    val projectId: String,
    @SerializedName("updatedAt")
    val updatedAt: String,
    @SerializedName("uploadStatus")
    val uploadStatus: String,
    @SerializedName("uploadedBy")
    val uploadedBy: TaskMemberDetail,
    @SerializedName("uploaderlocalFilePath")
    val uploaderLocalFilePath: String,
    @SerializedName("uploaderLocalId")
    val uploaderLocalId: String,
    @SerializedName("version")
    val version: Int
) : BaseResponse(), Parcelable

@Keep
@Parcelize
data class Floor(
    @SerializedName("_id")
    val _id: String,
    @SerializedName("floorName")
    val floorName: String
) : Parcelable
