package com.zstronics.ceibro.ui.tasks.newsubtask

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.interfaces.IBase

interface INewSubTask {
    interface State : IBase.State {
        var dueDate: String
        var startDate: String
        val subtaskTitle: MutableLiveData<String>
        val description: MutableLiveData<String>
        val doneImageRequired: MutableLiveData<Boolean>
        val doneCommentsRequired: MutableLiveData<Boolean>

    }

    interface ViewModel : IBase.ViewModel<State> {
    }
}