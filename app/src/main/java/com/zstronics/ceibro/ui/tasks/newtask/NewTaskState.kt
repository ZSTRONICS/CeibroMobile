package com.zstronics.ceibro.ui.tasks.newtask

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.state.BaseState
import javax.inject.Inject

class NewTaskState @Inject constructor() : BaseState(), INewTask.State {
    override var dueDate: String = ""
    override var startDate: String = ""
    override val taskTitle: MutableLiveData<String> = MutableLiveData("")
    override val isMultiTask: MutableLiveData<Boolean> = MutableLiveData(false)
}