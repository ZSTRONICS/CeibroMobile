package com.zstronics.ceibro.data.repos.projects.floor

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class CreateFloorResponseV2(
    @SerializedName("floor")
    var floor: FloorResponseV2? = null
) : BaseResponse(), Parcelable
