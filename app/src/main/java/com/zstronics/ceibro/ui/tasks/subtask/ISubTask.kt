package com.zstronics.ceibro.ui.tasks.subtask

import android.view.View
import android.widget.PopupWindow
import com.zstronics.ceibro.base.interfaces.IBase

interface ISubTask {
    interface State : IBase.State {
    }

    interface ViewModel : IBase.ViewModel<State> {
        fun getSubTasks()
        fun showSubtaskCardMenuPopup(v : View)
        fun popUpMenu(v : View): PopupWindow
    }
}