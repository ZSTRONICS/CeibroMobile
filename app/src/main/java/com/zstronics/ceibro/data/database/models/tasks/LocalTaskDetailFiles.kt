package com.zstronics.ceibro.data.database.models.tasks

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
@Keep
data class LocalTaskDetailFiles(
    @SerializedName("fileID")
    val fileID: String,
    @SerializedName("fileComment")
    val fileComment: String,
    @SerializedName("userComment")
    val userComment: String?,
    @SerializedName("fileName")
    val fileName: String,
    @SerializedName("fileTag")
    val fileTag: String,
    @SerializedName("fileUrl")
    val fileUrl: String,
    @SerializedName("hasComment")
    val hasComment: Boolean,
    @SerializedName("moduleId")
    val moduleId: String,
    @SerializedName("moduleType")
    val moduleType: String,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("uploadedBy")
    val uploadedBy: TaskMemberDetail,
    @SerializedName("isTaskFile")
    val isTaskFile: Boolean = false,
) : Parcelable
