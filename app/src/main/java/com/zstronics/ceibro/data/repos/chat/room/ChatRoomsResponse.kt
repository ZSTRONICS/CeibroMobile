package com.zstronics.ceibro.data.repos.chat.room

import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import java.util.*

data class ChatRoomsResponse(
    @SerializedName("userallchat")
    val chatRooms: ArrayList<ChatRoom>
) : BaseResponse()