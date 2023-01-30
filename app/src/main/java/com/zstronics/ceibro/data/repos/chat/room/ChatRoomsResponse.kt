package com.zstronics.ceibro.data.repos.chat.room


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse

@Keep
data class ChatRoomsResponse(
    @SerializedName("userallchat")
    val chatRooms: List<ChatRoom>
) : BaseResponse()