package com.zstronics.ceibro.data.repos.task.models.v2

import com.google.gson.annotations.SerializedName

class SyncTasksBody(
    @SerializedName("eventIds")
    var eventIds: List<String>? = emptyList()
)