package com.zstronics.ceibro.ui.tasks.v2.newtask

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.state.BaseState
import javax.inject.Inject

class NewTaskV2State @Inject constructor() : BaseState(), INewTaskV2.State {
    override var dueDate: String = ""
    override var taskTitle: MutableLiveData<String> = MutableLiveData("")
    override var description: MutableLiveData<String> = MutableLiveData("")
}