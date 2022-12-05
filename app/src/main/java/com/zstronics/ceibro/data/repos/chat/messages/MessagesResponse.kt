package com.zstronics.ceibro.data.repos.chat.messages

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import com.zstronics.ceibro.data.repos.chat.room.Member
import kotlinx.parcelize.Parcelize


@Keep
data class MessagesResponse(
    @SerializedName("message")
    val messages: List<ChatMessage>
) : BaseResponse() {
    @Keep
    @Parcelize
    data class ChatMessage(
        @SerializedName("answeredBy")
        val answeredBy: List<String>? = null,
        @SerializedName("chat")
        val chat: String? = null,
        @SerializedName("companyName")
        var companyName: String,
        @SerializedName("createdAt")
        val createdAt: String? = null,

        @SerializedName("_id")
        var id: String = "",
        @SerializedName("message")
        var message: String = "",
        @SerializedName("myMessage")
        var myMessage: Boolean = false,
        @SerializedName("pinnedBy")
        val pinnedBy: List<String>? = null,
        @SerializedName("readBy")
        val readBy: List<ReadBy>? = null,
        @SerializedName("receivedBy")
        val receivedBy: List<String>? = null,
        @SerializedName("seen")
        val seen: Boolean = false,
        @SerializedName("sender")
        var sender: Sender,
        @SerializedName("time")
        var time: String,
        @SerializedName("type")
        val type: String,
        @SerializedName("replyOf")
        var replyOf: ReplyOf? = null,
        @SerializedName("updatedAt")
        val updatedAt: String = "",
        @SerializedName("voiceUrl")
        val voiceUrl: String = "",
        @SerializedName("title")
        val title: String = ""
    ) : BaseResponse(), Parcelable {
        @Keep
        @Parcelize
        data class ReadBy(
            @SerializedName("firstName")
            val firstName: String,
            @SerializedName("id")
            val id: String,
            @SerializedName("surName")
            val surName: String
        ) : BaseResponse(), Parcelable

        @Keep
        @Parcelize
        data class Sender(
            @SerializedName("firstName")
            val firstName: String,
            @SerializedName("id")
            val id: String,
            @SerializedName("surName")
            val surName: String
        ) : BaseResponse(), Parcelable
    }

    @Parcelize
    data class ReplyOf(
        @SerializedName("access")
        val access: List<String>? = null,
        @SerializedName("answeredBy")
        val answeredBy: List<Member>? = null,
        @SerializedName("chat")
        val chat: String? = "",
        @SerializedName("id")
        var id: String? = null,
        @SerializedName("message")
        var message: String,
        @SerializedName("pinnedBy")
        val pinnedBy: List<String>? = null,
        @SerializedName("readBy")
        val readBy: List<String>? = null,
        @SerializedName("receivedBy")
        val receivedBy: List<String>? = null,
        @SerializedName("sender")
        val sender: String = "",
        @SerializedName("type")
        val type: String,
        @SerializedName("voiceUrl")
        val voiceUrl: String = ""
    ) : BaseResponse(), Parcelable
}