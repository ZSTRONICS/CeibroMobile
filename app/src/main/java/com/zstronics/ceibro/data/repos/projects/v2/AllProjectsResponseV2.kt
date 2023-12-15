package com.zstronics.ceibro.data.repos.projects.v2

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import com.zstronics.ceibro.data.database.models.projects.CeibroFloorV2
import com.zstronics.ceibro.data.database.models.projects.CeibroGroupsV2
import com.zstronics.ceibro.data.database.models.projects.CeibroProjectV2
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class AllProjectsResponseV2(
    @SerializedName("allProjects")
    val allProjects: List<CeibroProjectV2>,
    @SerializedName("allFloors")
    val allFloors: List<CeibroFloorV2>,
    @SerializedName("allGroups")
    val allGroups: List<CeibroGroupsV2>,
) : BaseResponse(), Parcelable
