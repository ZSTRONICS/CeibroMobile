package com.zstronics.ceibro.data.repos.chat.messages

import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.ui.enums.EventType
import com.zstronics.ceibro.ui.enums.MessageType
import org.json.JSONObject

data class SocketMessageRequest(
    @SerializedName("eventType") var eventType: String?,
    @SerializedName("data") var data: String?
)

