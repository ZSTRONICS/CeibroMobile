package com.zstronics.ceibro.data.repos.projects.v2

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import com.zstronics.ceibro.data.database.models.projects.CeibroProjectV2
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class NewProjectResponseV2(
    @SerializedName("newProject")
    val newProject: CeibroProjectV2
) : BaseResponse(), Parcelable
