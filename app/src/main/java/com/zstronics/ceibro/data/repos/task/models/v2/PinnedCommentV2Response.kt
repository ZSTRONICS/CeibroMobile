package com.zstronics.ceibro.data.repos.task.models.v2

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import kotlinx.parcelize.Parcelize

@Parcelize
@Keep
data class PinnedCommentV2Response(
    @SerializedName("data")
    val data: CommentPinnedData
) : BaseResponse(), Parcelable {

    @Parcelize
    @Keep
    data class CommentPinnedData(
        @SerializedName("taskId")
        val taskId: String,
        @SerializedName("eventId")
        val eventId: String,
        @SerializedName("updatedAt")
        val updatedAt: String,
        @SerializedName("isPinned")
        val isPinned: Boolean
    ) : Parcelable
}