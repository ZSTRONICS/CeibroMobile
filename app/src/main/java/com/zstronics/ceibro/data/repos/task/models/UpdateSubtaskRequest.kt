package com.zstronics.ceibro.data.repos.task.models


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep

@Keep
data class UpdateSubtaskRequest(
    @SerializedName("description") val description: String
)