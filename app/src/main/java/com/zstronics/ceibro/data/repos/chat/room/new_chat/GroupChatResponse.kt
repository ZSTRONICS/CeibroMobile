package com.zstronics.ceibro.data.repos.chat.room.new_chat

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import com.zstronics.ceibro.data.repos.chat.room.ChatRoom
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class GroupChatResponse(
    @SerializedName("newChat") val newChat: ChatRoom
) : BaseResponse(), Parcelable