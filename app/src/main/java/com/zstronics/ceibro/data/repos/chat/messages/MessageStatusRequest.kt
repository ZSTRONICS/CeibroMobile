package com.zstronics.ceibro.data.repos.chat.messages

import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.ui.enums.MessageType

data class MessageStatusRequest(
    @SerializedName("userId") var userId: String?,
    @SerializedName("roomId") var roomId: String?,
    @SerializedName("messageId") var messageId: String?
)

