package com.zstronics.ceibro.data.repos.projects.group

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import kotlinx.parcelize.Parcelize


@Keep
@Parcelize
data class GroupResponseV2(

    @SerializedName("projectId") var projectId: String? = null,
    @SerializedName("groupName") var groupName: String? = null,
    @SerializedName("creator") var creator: String? = null,
    @SerializedName("drawings") var drawings: ArrayList<String> = arrayListOf(),
    @SerializedName("_id") var Id: String? = null,
    @SerializedName("createdAt") var createdAt: String? = null,
    @SerializedName("updatedAt") var updatedAt: String? = null

) : BaseResponse(), Parcelable