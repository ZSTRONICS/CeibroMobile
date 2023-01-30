package com.zstronics.ceibro.data.repos.chat.messages

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.ui.enums.MessageType

@Keep
data class MessageStatusRequest(
    @SerializedName("userId") var userId: String?,
    @SerializedName("roomId") var roomId: String?,
    @SerializedName("messageId") var messageId: String?
)

