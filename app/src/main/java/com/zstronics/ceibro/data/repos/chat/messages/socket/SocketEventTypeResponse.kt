package com.zstronics.ceibro.data.repos.chat.messages.socket

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class SocketEventTypeResponse (
    @SerializedName("eventType")
    val eventType: String
)