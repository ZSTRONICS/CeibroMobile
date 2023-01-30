package com.zstronics.ceibro.data.repos.chat.messages

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.repos.chat.questionarie.Question

@Keep
data class QuestionRequest(
    @SerializedName("members")
    val members: List<String>?,
    @SerializedName("dueDate")
    val dueDate: String,
    @SerializedName("questions")
    val questions: ArrayList<Question>?,
    @SerializedName("chat")
    val chat: String?,
    @SerializedName("chat")
    val title: String?,
)