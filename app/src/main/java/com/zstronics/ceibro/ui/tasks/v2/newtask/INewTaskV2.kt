package com.zstronics.ceibro.ui.tasks.v2.newtask

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.interfaces.IBase
import com.zstronics.ceibro.data.database.models.tasks.CeibroTask

interface INewTaskV2 {
    interface State : IBase.State {
        var dueDate: String
        var taskTitle: MutableLiveData<String>
        var description: MutableLiveData<String>
    }

    interface ViewModel : IBase.ViewModel<State> {
    }
}