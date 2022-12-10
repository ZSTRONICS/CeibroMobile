package com.zstronics.ceibro.data.repos.chat.messages


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class NewGroupChatRequest(
    @SerializedName("members") val members: List<String>,
    @SerializedName("name") val name: String?,
    @SerializedName("projectId") val projectId: String
)