package com.zstronics.ceibro.ui.tasks.newsubtask

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.state.BaseState
import javax.inject.Inject

class NewSubTaskState @Inject constructor() : BaseState(), INewSubTask.State {
    override var dueDate: String = ""
    override var startDate: String = ""
    override val taskTitle: MutableLiveData<String> = MutableLiveData("")
    override val description: MutableLiveData<String> = MutableLiveData("")
    override val doneImageRequired: MutableLiveData<Boolean> = MutableLiveData(true)
    override val doneCommentsRequired: MutableLiveData<Boolean> = MutableLiveData(true)
}