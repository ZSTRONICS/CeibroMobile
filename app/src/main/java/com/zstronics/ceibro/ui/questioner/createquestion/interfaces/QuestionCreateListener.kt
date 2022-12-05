package com.zstronics.ceibro.ui.questioner.createquestion.interfaces

interface QuestionCreateListener {
    fun onQuestionTypeChange(position: Int, type: String)
    fun onQuestionTitleChange(position: Int, title: String)
    fun onQuestionChoiceTitleChange(questionPosition: Int, position: Int, title: String)
    fun onQuestionChoiceDelete(questionPosition: Int, position: Int)
    fun onQuestionChoiceAdd(questionPosition: Int)
}