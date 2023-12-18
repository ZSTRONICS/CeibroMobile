package com.zstronics.ceibro.data.repos.projects.drawing

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize


@Keep
@Parcelize
data class UploadDrawingV2FileMetaData(
    @SerializedName("fileName")
    val fileName: String,
    @SerializedName("tag")
    val tag: String,
    @SerializedName("uploaderlocalFilePath")
    val uploaderLocalFilePath: String,
    @SerializedName("uploaderLocalId")
    val uploaderLocalId: String
) : Parcelable
