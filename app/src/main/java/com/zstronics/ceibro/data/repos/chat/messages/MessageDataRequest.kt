package com.zstronics.ceibro.data.repos.chat.messages

import com.google.gson.annotations.SerializedName

data class MessageDataRequest(
    @SerializedName("userId") var userId: String?,
    @SerializedName("message") var message: String?
)
