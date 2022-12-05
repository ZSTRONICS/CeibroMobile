package com.zstronics.ceibro.ui.questioner.createquestion

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.state.BaseState
import com.zstronics.ceibro.data.repos.chat.room.Member
import javax.inject.Inject

class CreateQuestionState @Inject constructor() : BaseState(), ICreateQuestion.State {
    override val questionTitle: MutableLiveData<String> = MutableLiveData("")
    override var dueDate: String = ""
    override val assignee: ArrayList<String> = arrayListOf()
    override val participants: MutableLiveData<ArrayList<Member>> = MutableLiveData(arrayListOf())
}