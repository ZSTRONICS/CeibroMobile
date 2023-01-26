package com.zstronics.ceibro.data.repos.chat.messages.socket

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class SocketEventTypeResponse(
    @SerializedName("module")
    val module: String,
    @SerializedName("eventType")
    val eventType: String
)