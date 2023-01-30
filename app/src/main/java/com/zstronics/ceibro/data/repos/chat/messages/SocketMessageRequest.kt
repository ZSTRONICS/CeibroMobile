package com.zstronics.ceibro.data.repos.chat.messages

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class SocketMessageRequest(
    @SerializedName("eventType") var eventType: String?,
    @SerializedName("data") var data: String?
)

