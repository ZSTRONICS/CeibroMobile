package com.zstronics.ceibro.data.repos.chat.messages.socket


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep

@Keep
data class AllMessageSeenSocketResponse(
    @SerializedName("data") val `data`: Data,
    @SerializedName("eventType") val eventType: String
) {
    @Keep
    data class Data(
        @SerializedName("roomId") val roomId: String,
        @SerializedName("userId") val userId: String
    )
}