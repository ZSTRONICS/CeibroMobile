package com.zstronics.ceibro.data.repos.projects.drawing


import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import com.zstronics.ceibro.data.database.models.projects.DrawingV2
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class UploadDrawingV2Response(
    @SerializedName("message")
    val message: String,
    @SerializedName("drawings")
    val drawings: List<DrawingV2> = listOf(),
    @SerializedName("groupUpdatedAt")
    val groupUpdatedAt: String,
    @SerializedName("floorUpdatedAt")
    val floorUpdatedAt: String
) : BaseResponse(), Parcelable