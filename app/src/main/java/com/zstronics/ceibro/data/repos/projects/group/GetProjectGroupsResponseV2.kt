package com.zstronics.ceibro.data.repos.projects.group

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class GetProjectGroupsResponseV2(
    @SerializedName("groups")
    var groups: ArrayList<GroupResponseV2> = arrayListOf()
) : BaseResponse(), Parcelable