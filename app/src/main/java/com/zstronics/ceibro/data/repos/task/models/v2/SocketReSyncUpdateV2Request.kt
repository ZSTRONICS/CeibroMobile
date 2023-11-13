package com.zstronics.ceibro.data.repos.task.models.v2

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class SocketReSyncUpdateV2Request(
    @SerializedName("missingEventIds")
    val missingEventIds: List<String>,
    @SerializedName("missingTaskIds")
    val missingTaskIds: List<String>
): Parcelable
