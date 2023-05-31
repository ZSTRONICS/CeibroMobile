package com.zstronics.ceibro.data.repos.task.models


import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class NewTopicResponse(
    @SerializedName("newTopic")
    val newTopic: TopicsResponse.TopicData
) : BaseResponse(), Parcelable