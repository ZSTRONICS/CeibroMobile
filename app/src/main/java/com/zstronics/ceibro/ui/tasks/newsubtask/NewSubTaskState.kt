package com.zstronics.ceibro.ui.tasks.newsubtask

import com.zstronics.ceibro.base.state.BaseState
import javax.inject.Inject

class NewSubTaskState @Inject constructor() : BaseState(), INewSubTask.State {
    override var dueDate: String = ""
    override var startDate: String = ""
}