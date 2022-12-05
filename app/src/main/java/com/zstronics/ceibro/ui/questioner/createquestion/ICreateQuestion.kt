package com.zstronics.ceibro.ui.questioner.createquestion

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.interfaces.IBase
import com.zstronics.ceibro.data.repos.chat.questionarie.Question
import com.zstronics.ceibro.data.repos.chat.room.Member

interface ICreateQuestion {
    interface State : IBase.State {
        val questionTitle: MutableLiveData<String>
        var dueDate: String
        val assignee: ArrayList<String>
        val participants: MutableLiveData<ArrayList<Member>>
    }

    interface ViewModel : IBase.ViewModel<State> {
        val questions: MutableLiveData<ArrayList<Question>>
    }
}