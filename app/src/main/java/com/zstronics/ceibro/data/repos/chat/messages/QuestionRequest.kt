package com.zstronics.ceibro.data.repos.chat.messages

import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.repos.chat.questionarie.Question

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