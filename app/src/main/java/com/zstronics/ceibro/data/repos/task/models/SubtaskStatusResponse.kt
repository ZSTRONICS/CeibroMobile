package com.zstronics.ceibro.data.repos.task.models


import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import kotlinx.parcelize.Parcelize

@Parcelize
@Keep
data class SubtaskStatusResponse(
    @SerializedName("result")
    val result: List<SubtaskStatusData>?
) : BaseResponse(), Parcelable