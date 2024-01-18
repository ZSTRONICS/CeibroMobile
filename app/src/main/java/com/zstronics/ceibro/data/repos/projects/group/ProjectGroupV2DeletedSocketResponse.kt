package com.zstronics.ceibro.data.repos.projects.group


import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import kotlinx.parcelize.Parcelize

@Keep
data class ProjectGroupV2DeletedSocketResponse(
    @SerializedName("data") val `data`: ProjectGroupIdClass,
    @SerializedName("eventType") val eventType: String,
    @SerializedName("module") val module: String
): BaseResponse()


@Keep
@Parcelize
data class ProjectGroupIdClass(
    @SerializedName("removedGroupId") val removedGroupId: String
): Parcelable

