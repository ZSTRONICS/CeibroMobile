package com.zstronics.ceibro.data.repos.projects.projectsmain


import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import com.zstronics.ceibro.data.repos.projects.floor.FloorResponseV2
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class ProjectFloorResponseV2(
    @SerializedName("floors")
    val floors: List<FloorResponseV2>?= arrayListOf()
) : BaseResponse(), Parcelable
