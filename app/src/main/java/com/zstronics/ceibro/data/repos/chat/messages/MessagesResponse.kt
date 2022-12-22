package com.zstronics.ceibro.data.repos.chat.messages

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
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
        @SerializedName("createdAt")
        var createdAt: String? = null,
        @SerializedName("questions")
        val questions: List<String>? = null,
        @SerializedName("access")
        val access: List<String>? = null,
        @SerializedName("files")
        val files: List<String>? = null,
        @SerializedName("_id")
        var id: String = "",
        @SerializedName("message")
        var message: String = "",
        @SerializedName("pinnedBy")
        val pinnedBy: List<String>? = null,
        @SerializedName("readBy")
        var readBy: List<ReadBy>? = null,
        @SerializedName("receivedBy")
        val receivedBy: List<String>? = null,
        @SerializedName("sender")
        var sender: Sender,
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
            val surName: String,
            @SerializedName("profilePic")
            val profilePic: String?,
            @SerializedName("companyName")
            val companyName: String?
        ) : BaseResponse(), Parcelable

        @Keep
        @Parcelize
        data class Sender(
            @SerializedName("firstName")
            val firstName: String,
            @SerializedName("id")
            val id: String,
            @SerializedName("surName")
            val surName: String,
            @SerializedName("profilePic")
            val profilePic: String?,
            @SerializedName("companyName")
            val companyName: String?
        ) : BaseResponse(), Parcelable
    }

    @Parcelize
    data class ReplyOf(
        @SerializedName("_id")
        val id: String,
        @SerializedName("message")
        val message: String,
        @SerializedName("sender")
        val replySender: ReplySender,
        @SerializedName("type")
        val type: String
    ) : BaseResponse(), Parcelable {
        @Keep
        @Parcelize
        data class ReplySender(
            @SerializedName("firstName")
            val firstName: String,
            @SerializedName("id")
            val id: String,
            @SerializedName("surName")
            val surName: String,
            @SerializedName("profilePic")
            val profilePic: String?,
            @SerializedName("companyName")
            val companyName: String?
        ) : BaseResponse(), Parcelable
    }
}