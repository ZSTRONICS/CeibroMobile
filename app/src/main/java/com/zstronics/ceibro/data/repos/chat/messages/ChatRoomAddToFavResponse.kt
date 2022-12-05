package com.zstronics.ceibro.data.repos.chat.messages

import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse

data class ChatRoomAddToFavResponse(
    @SerializedName("message") val message: String
):BaseResponse()