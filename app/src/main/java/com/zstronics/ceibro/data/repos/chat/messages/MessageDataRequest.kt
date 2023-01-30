package com.zstronics.ceibro.data.repos.chat.messages

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class MessageDataRequest(
    @SerializedName("userId") var userId: String?,
    @SerializedName("message") var message: String?
)
