package com.zstronics.ceibro.data.database.models.attachments

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import com.zstronics.ceibro.data.database.TableNames
import kotlinx.parcelize.Parcelize

@Entity(tableName = TableNames.FilesAttachments)
@Keep
@Parcelize
data class FilesAttachments(
    @PrimaryKey
    @SerializedName("_id") val id: String,
    @SerializedName("access") val access: List<String>,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("fileName") val fileName: String,
    @SerializedName("fileType") val fileType: String,
    @SerializedName("fileUrl") val fileUrl: String,
    @SerializedName("moduleId") val moduleId: String,
    @SerializedName("moduleType") val moduleType: String,
    @SerializedName("updatedAt") val updatedAt: String,
    @SerializedName("uploadStatus") val uploadStatus: String,
    @SerializedName("uploadedBy") val uploadedBy: String,
    @SerializedName("version") val version: Int,
    @SerializedName("fileSize") val fileSize: Int
) : BaseResponse(), Parcelable