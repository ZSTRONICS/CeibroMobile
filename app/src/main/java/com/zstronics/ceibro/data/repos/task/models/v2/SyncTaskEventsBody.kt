package com.zstronics.ceibro.data.repos.task.models.v2

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
class SyncTaskEventsBody(
    @SerializedName("eventIds")
    var eventIds: List<Int> = emptyList()
)