package com.zstronics.ceibro.data.repos.projects.group


import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import com.zstronics.ceibro.data.database.models.projects.CeibroGroupsV2
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class CreateGroupResponseV2(
    @SerializedName("group")
    var group: CeibroGroupsV2
) : BaseResponse(), Parcelable

