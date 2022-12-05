package com.zstronics.ceibro.data.repos.chat.messages

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class NewMessageResponse(
    @SerializedName("message")
    val message: MessagesResponse.ChatMessage
) : BaseResponse(), Parcelable
