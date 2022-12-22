package com.zstronics.ceibro.data.repos.chat.messages.socket


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep
import com.zstronics.ceibro.data.repos.chat.messages.MessagesResponse
import com.zstronics.ceibro.data.repos.chat.room.Member

@Keep
data class MessageSeenSocketResponse(
    @SerializedName("data") val `data`: Data,
    @SerializedName("eventType") val eventType: String
) {
    @Keep
    data class Data(
        @SerializedName("roomId") val roomId: String,
        @SerializedName("updatedMessage") val updatedMessage: List<UpdatedMessage>,
        @SerializedName("userId") val userId: String
    ) {
        @Keep
        data class UpdatedMessage(
            @SerializedName("_id") val id: String,
            @SerializedName("readBy") val readBy: List<MessagesResponse.ChatMessage.ReadBy>
        )
    }
}