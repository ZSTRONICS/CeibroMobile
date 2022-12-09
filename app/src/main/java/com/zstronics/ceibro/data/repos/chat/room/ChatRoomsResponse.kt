package com.zstronics.ceibro.data.repos.chat.room


import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse

data class ChatRoomsResponse(
    @SerializedName("userallchat")
    val chatRooms: List<ChatRoom>
) : BaseResponse()