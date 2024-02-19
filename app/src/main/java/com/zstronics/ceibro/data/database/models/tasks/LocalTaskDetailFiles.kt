package com.zstronics.ceibro.data.database.models.tasks

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.Entity
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.database.TableNamesV2
import kotlinx.parcelize.Parcelize

@Entity(tableName = TableNamesV2.TaskFiles, primaryKeys = ["taskId"])
@Parcelize
@Keep
data class LocalTaskDetailFiles(
    @SerializedName("taskId")
    val taskId: String,
    @SerializedName("commentId")
    val commentId: String,
    @SerializedName("fileId")
    val fileId: String,
    @SerializedName("comment")
    val comment: String,
    @SerializedName("fileName")
    val fileName: String,
    @SerializedName("fileTag")
    val fileTag: String,
    @SerializedName("fileUrl")
    val fileUrl: String,
    @SerializedName("fileType")
    val fileType: String,
    @SerializedName("fileSize")
    val fileSize: String,
    @SerializedName("hasComment")
    val hasComment: Boolean,
    @SerializedName("moduleId")
    val moduleId: String,
    @SerializedName("moduleType")
    val moduleType: String,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("initiator")
    val initiator: TaskMemberDetail,
    @SerializedName("isTaskFile")
    val isTaskFile: Boolean,
    @SerializedName("isCommentFile")
    val isCommentFile: Boolean,
) : Parcelable
