package com.zstronics.ceibro.ui.tasks.taskdetailview

import android.view.View
import android.widget.PopupWindow
import com.zstronics.ceibro.base.interfaces.IBase

interface ITaskDetail {
    interface State : IBase.State {
    }

    interface ViewModel : IBase.ViewModel<State> {
        fun getSubTasks(taskId: String)
        fun showSubtaskCardMenuPopup(v : View)
        fun popUpMenu(v : View): PopupWindow
    }
}