package com.zstronics.ceibro.data.repos.task.models


import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import com.zstronics.ceibro.data.database.TableNamesV2
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

@Entity(tableName = TableNamesV2.Topics)
@Keep
data class TopicsV2DatabaseEntity(
    @PrimaryKey
    val id: Int,
    @ColumnInfo("topicsData")
    val topicsData: TopicsResponse
)