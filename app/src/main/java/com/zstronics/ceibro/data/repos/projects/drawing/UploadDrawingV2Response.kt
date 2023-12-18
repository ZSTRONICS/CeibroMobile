package com.zstronics.ceibro.data.repos.projects.drawing


import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import com.zstronics.ceibro.data.database.models.tasks.TaskMemberDetail
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class UploadDrawingV2Response(
    @SerializedName("message")
    val message: String,
    @SerializedName("drawings")
    val drawings: List<DrawingV2> = listOf(),
    @SerializedName("floorUpdatedAt")
    val floorUpdatedAt: String,
    @SerializedName("groupUpdatedAt")
    val groupUpdatedAt: String

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
    val fileUrl: String = "",
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
) : Parcelable

@Keep
@Parcelize
data class Floor(
    @SerializedName("_id")
    val _id: String,
    @SerializedName("floorName")
    val floorName: String
) : Parcelable
