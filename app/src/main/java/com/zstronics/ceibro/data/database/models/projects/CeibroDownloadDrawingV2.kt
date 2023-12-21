package com.zstronics.ceibro.data.database.models.projects

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.Entity
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.database.TableNamesV2
import com.zstronics.ceibro.data.repos.projects.drawing.DrawingV2
import kotlinx.parcelize.Parcelize


@Keep
@Parcelize
@Entity(tableName = TableNamesV2.DownloadDrawing, primaryKeys = ["drawingId"])
data class CeibroDownloadDrawingV2(
    @SerializedName("downloading")
    var downloading: Boolean = false,
    @SerializedName("isDownloaded")
    var isDownloaded: Boolean = false,
    @SerializedName("downloadId")
    var downloadId: Long,
    @SerializedName("drawing")
    var drawing: DrawingV2,
    @SerializedName("groupId")
    val groupId: String,
    @SerializedName("drawingId")
    val drawingId: String,
    @SerializedName("localUri")
    var localUri: String
) : Parcelable
