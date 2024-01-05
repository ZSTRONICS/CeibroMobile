package com.zstronics.ceibro.data.repos.location

import android.graphics.Bitmap
import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import com.zstronics.ceibro.data.database.models.projects.CeibroFloorV2
import com.zstronics.ceibro.data.database.models.projects.CeibroGroupsV2
import com.zstronics.ceibro.data.database.models.projects.CeibroProjectV2
import com.zstronics.ceibro.data.database.models.tasks.CeibroDrawingPins
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class MarkerPointsData(
    @SerializedName("xPointToDisplay")
    val xPointToDisplay: Float,
    @SerializedName("yPointToDisplay")
    val yPointToDisplay: Float,
    @SerializedName("actualEventX")
    val actualEventX: Float,
    @SerializedName("actualEventY")
    val actualEventY: Float,
    @SerializedName("isNewPoint")
    val isNewPoint: String,
    @SerializedName("loadedPointData")
    val loadedPinData: CeibroDrawingPins?,
    @SerializedName("loadedPointData")
    val loadedBitmap: Bitmap?
) : Parcelable
