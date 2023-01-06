package com.zstronics.ceibro.ui.tasks.newtask

import com.zstronics.ceibro.base.state.BaseState
import javax.inject.Inject

class NewTaskState @Inject constructor() : BaseState(), INewTask.State {
    override var dueDate: String = ""
}