package com.zstronics.ceibro.data.repos.chat.messages

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class SocketReceiveMessageResponse (
    @SerializedName("eventType")
    val eventType: String,
    @SerializedName("data")
    val data: ReceivedData
): BaseResponse(), Parcelable {
    @Keep
    @Parcelize
    data class ReceivedData(
        @SerializedName("myId")
        val myId: String,
        @SerializedName("data")
        val messageData: ReceivedMessageData,
    ) : BaseResponse(), Parcelable {
        @Keep
        @Parcelize
        data class ReceivedMessageData(
            @SerializedName("message")
            val message: MessagesResponse.ChatMessage,
            @SerializedName("from")
            val from: String,
            @SerializedName("chat")
            val chat: String,
            @SerializedName("mutedFor")
            val mutedFor: List<String>? = null,
        ) : BaseResponse(), Parcelable
    }
}
