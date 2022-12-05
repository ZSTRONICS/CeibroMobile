package com.zstronics.ceibro.data.repos.chat.messages

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class NewSocketReceiveMessageResponse (
    @SerializedName("myId")
    val myId: String,
    @SerializedName("data")
    val data: ReceivedMessageDataResponse,
): BaseResponse(), Parcelable {
    @Keep
    @Parcelize
    data class ReceivedMessageDataResponse(
        @SerializedName("message")
        val message: MessagesResponse.ChatMessage,
        @SerializedName("from")
        val from: String,
        @SerializedName("chat")
        val chat: String
    ) : BaseResponse(), Parcelable
}
