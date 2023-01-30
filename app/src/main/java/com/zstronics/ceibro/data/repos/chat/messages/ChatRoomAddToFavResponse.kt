package com.zstronics.ceibro.data.repos.chat.messages

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse

@Keep
data class ChatRoomAddToFavResponse(
    @SerializedName("message") val message: String
):BaseResponse()