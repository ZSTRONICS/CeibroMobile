package com.zstronics.ceibro.data.repos.chat.messages

import com.google.gson.annotations.SerializedName

data class SocketMessageRequest(
    @SerializedName("eventType") var eventType: String?,
    @SerializedName("data") var data: String?
)

