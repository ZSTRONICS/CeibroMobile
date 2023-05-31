package com.zstronics.ceibro.data.repos.task.models


import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class TopicsResponse(
    @SerializedName("allTopics")
    val allTopics: List<TopicData>,
    @SerializedName("recentTopics")
    val recentTopics: List<TopicData>
) : BaseResponse(), Parcelable {
    @Keep
    @Parcelize
    data class TopicData(
        @SerializedName("_id")
        val id: String,
        @SerializedName("topic")
        val topic: String,
        @SerializedName("userId")
        val userId: String
    ) : BaseResponse(), Parcelable
}