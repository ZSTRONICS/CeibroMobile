package com.zstronics.ceibro.data.repos.chat.questionarie

import com.google.gson.annotations.SerializedName

data class Question(
    @SerializedName("_id")
    val id: Int? = 0,
    @SerializedName("type")
    var questionType: String = "multiple",
    @SerializedName("question")
    var questionTitle: String = "",
    @SerializedName("options")
    val options: ArrayList<String> = arrayListOf("")
)
