package com.zstronics.ceibro.data.repos.projects.projectsmain


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.database.models.projects.CeibroProjectV2

@Keep
data class ProjectV2CreatedUpdatedSocketResponse(
    @SerializedName("data") val `data`: CeibroProjectV2,
    @SerializedName("eventType") val eventType: String,
    @SerializedName("module") val module: String
)