package com.zstronics.ceibro.ui.tasks.newtask

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.interfaces.IBase
import com.zstronics.ceibro.data.database.models.tasks.CeibroTask

interface INewTask {
    interface State : IBase.State {
        var dueDate: String
        var startDate: String
        var taskTitle: MutableLiveData<String>
        var description: MutableLiveData<String>
        val isMultiTask: MutableLiveData<Boolean>
    }

    interface ViewModel : IBase.ViewModel<State> {
        fun loadProjects()
        fun loadProjects(taskParcel: CeibroTask)
    }
}