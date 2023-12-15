package com.zstronics.ceibro.data.repos.projects.projectsmain


import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import com.zstronics.ceibro.data.database.models.projects.CeibroFloorV2
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class ProjectFloorResponseV2(
    @SerializedName("floors")
    val floors: List<CeibroFloorV2>?= arrayListOf()
) : BaseResponse(), Parcelable
