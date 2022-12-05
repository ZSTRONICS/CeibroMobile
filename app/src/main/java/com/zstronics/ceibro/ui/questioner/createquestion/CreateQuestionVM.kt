package com.zstronics.ceibro.ui.questioner.createquestion

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.repos.chat.IChatRepository
import com.zstronics.ceibro.data.repos.chat.messages.QuestionRequest
import com.zstronics.ceibro.data.repos.chat.questionarie.Question
import com.zstronics.ceibro.data.repos.chat.room.ChatRoom
import com.zstronics.ceibro.ui.questioner.createquestion.adapter.QuestionsAdapter
import com.zstronics.ceibro.ui.questioner.createquestion.interfaces.QuestionCreateListener
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CreateQuestionVM @Inject constructor(
    override val viewState: CreateQuestionState,
    private val chatRepository: IChatRepository
) : HiltBaseViewModel<ICreateQuestion.State>(), ICreateQuestion.ViewModel, QuestionCreateListener {

    private val _questions: MutableLiveData<ArrayList<Question>> = MutableLiveData(arrayListOf())
    override val questions: MutableLiveData<ArrayList<Question>> = _questions

    @Inject
    lateinit var adapter: QuestionsAdapter
    var chatRoom: ChatRoom? = null

    override fun handleOnClick(id: Int) {
        super.handleOnClick(id)
        when (id) {
            R.id.addQuestion -> addNewQuestion()
        }
    }

    override fun onFirsTimeUiCreate(bundle: Bundle?) {
        super.onFirsTimeUiCreate(bundle)
        val id = getId()
        val question = Question(id = id, questionType = "multiple")
        _questions.value = arrayListOf(question)
        chatRoom = bundle?.getParcelable("chatRoom")
    }

    private fun addNewQuestion() {
        val id = getId()
        val question = Question(id = id, questionType = "multiple")
        val oldArray = _questions.value
        oldArray?.add(question)
        _questions.value = oldArray
    }

    private fun getId() = questions.value?.size?.plus(1)
    override fun onQuestionTypeChange(position: Int, type: String) {
        _questions.value?.get(position)?.questionType = type
    }

    override fun onQuestionTitleChange(position: Int, title: String) {
        _questions.value?.get(position)?.questionTitle = title
    }

    override fun onQuestionChoiceTitleChange(questionPosition: Int, position: Int, title: String) {
        val oldArray = _questions.value
        oldArray?.get(questionPosition)?.options?.set(position, title)
        _questions.value = oldArray
    }

    override fun onQuestionChoiceDelete(questionPosition: Int, position: Int) {
        val oldArray = _questions.value
        if (oldArray?.get(questionPosition)?.options?.size != 0)
            oldArray?.get(questionPosition)?.options?.removeAt(position)
        _questions.value = oldArray
    }

    override fun onQuestionChoiceAdd(questionPosition: Int) {
        val oldArray = _questions.value
        oldArray?.get(questionPosition)?.options?.add("")
        _questions.value = oldArray
    }

    fun onSave() {
        viewState.assignee.clear()
        val assigneeList = viewState.participants.value?.map { it.id }
        assigneeList?.let { viewState.assignee.addAll(it) }

        val questionRequest = QuestionRequest(
            members = assigneeList,
            dueDate = viewState.dueDate,
            questions = questions.value,
            chat = chatRoom?.id,
            title = viewState.questionTitle.value
        )
        loading(true)

        launch {
            when (val response = chatRepository.postQuestion(questionRequest)) {

                is ApiResponse.Success -> {
                    loading(false)
                    val data = response.data
                    handleOnClick(111)
                }

                is ApiResponse.Error -> {
                    loading(false)
                }
            }
        }

    }

}