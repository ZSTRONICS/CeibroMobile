package com.zstronics.ceibro.data.repos.task.models


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep

@Keep
data class NewTopicCreateRequest(
    @SerializedName("topic") val topic: String
)