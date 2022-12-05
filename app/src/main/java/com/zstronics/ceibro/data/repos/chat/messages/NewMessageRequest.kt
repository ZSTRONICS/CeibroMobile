package com.zstronics.ceibro.data.repos.chat.messages

import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.ui.enums.MessageType

data class NewMessageRequest(
    @SerializedName("message") var message: String?,
    @SerializedName("chat") var chat: String?,
    @Transient
    var messageType: MessageType = MessageType.MESSAGE,
    @SerializedName("type") var type: String? = messageType.name.lowercase(),
    @SerializedName("messageId") var messageId: String? = null,
    @SerializedName("myId") var myId: String? = null,
    @SerializedName("files") var files: ArrayList<MediaMessage?>? = null
)

data class MediaMessage(
    @SerializedName("fileType") var fileType: String?,
    @SerializedName("fileName") var fileName: String?,
    @SerializedName("url") var url: String?,
)
