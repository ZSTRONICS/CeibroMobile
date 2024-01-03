package com.zstronics.ceibro.data.repos.task.models.v2


import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.io.File

@Keep
@Parcelize
data class PinsMetaData(
    @SerializedName("page_width")
    val page_width: Float,
    @SerializedName("page_height")
    val page_height: Float,
    @SerializedName("x_coord")
    val x_coord: Float,
    @SerializedName("y_coord")
    val y_coord: Float,
    @SerializedName("creator")
    val creator: String,
    @SerializedName("drawingId")
    val drawingId: String
) : Parcelable