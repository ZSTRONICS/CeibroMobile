package com.zstronics.ceibro.data.repos.task.models.v2

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class SocketReSyncV2Response(
    @SerializedName("data")
    val data: UpdateRequiredEvents
) : BaseResponse(), Parcelable

@Keep
@Parcelize
data class UpdateRequiredEvents(
    @SerializedName("isUpdateContacts")
    val isUpdateContacts: Boolean,
    @SerializedName("isUpdateTopics")
    val isUpdateTopics: Boolean,
    @SerializedName("isUpdateUser")
    val isUpdateUser: Boolean,
    @SerializedName("missingEventIds")
    val missingEventIds: List<String>,
    @SerializedName("missingTaskIds")
    val missingTaskIds: List<String>
) : BaseResponse(), Parcelable