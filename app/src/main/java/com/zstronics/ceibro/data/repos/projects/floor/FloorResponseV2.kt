package com.zstronics.ceibro.data.repos.projects.floor

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import kotlinx.parcelize.Parcelize


@Keep
@Parcelize
data class FloorResponseV2(
    @SerializedName("_id")
    val id: String,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("creator")
    val creator: String,
    @SerializedName("deleted")
    val deleted: Boolean,
    @SerializedName("drawings")
    val drawings: List<String>,
    @SerializedName("floorName")
    val floorName: String,
    @SerializedName("projectId")
    val projectId: String,
    @SerializedName("updatedAt")
    val updatedAt: String,
    @SerializedName("__v")
    val v: Int,

    ) : BaseResponse(), Parcelable