package com.zstronics.ceibro.ui.locationv2.usage


import android.graphics.Bitmap
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize
import java.io.File
/*

@Keep
@Parcelize
data class AddLocationTask(
    @SerializedName("xCord") val xCord: Float,
    @SerializedName("yCord") val yCord: Float,
    @SerializedName("pageWidth") val pageWidth: Float,
    @SerializedName("pageHeight") val pageHeight: Float,
    @SerializedName("locationImgBitmap") val locationImgBitmap: Bitmap,
    @SerializedName("locationImgFile") val locationImgFile: File,
    @SerializedName("drawingId") val drawingId: String?,
    @SerializedName("drawingName") val drawingName: String,
    @SerializedName("projectId") val projectId: String?,
    @SerializedName("groupId") val groupId: String?
): Parcelable@Keep
*/

@Parcelize
data class AddLocationTask(
    @SerializedName("xCord") val xCord: Float,
    @SerializedName("yCord") val yCord: Float,
    @SerializedName("pageWidth") val pageWidth: Float,
    @SerializedName("pageHeight") val pageHeight: Float,
    @SerializedName("locationImgFile") val locationImgFile: File,
    @SerializedName("drawingId") val drawingId: String?,
    @SerializedName("drawingName") val drawingName: String,
    @SerializedName("projectId") val projectId: String?,
    @SerializedName("groupId") val groupId: String?
): Parcelable