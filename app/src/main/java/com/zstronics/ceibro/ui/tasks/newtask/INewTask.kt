package com.zstronics.ceibro.ui.tasks.newtask

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.interfaces.IBase

interface INewTask {
    interface State : IBase.State {
        var dueDate: String
        var startDate: String
        val taskTitle: MutableLiveData<String>
        val description: MutableLiveData<String>
        val isMultiTask: MutableLiveData<Boolean>
    }

    interface ViewModel : IBase.ViewModel<State> {
        fun loadProjects()
    }
}