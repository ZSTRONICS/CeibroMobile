package com.zstronics.ceibro.ui.tasks.subtaskcomments

import com.zstronics.ceibro.base.interfaces.IBase

interface ISubTaskComments {
    interface State : IBase.State {
    }

    interface ViewModel : IBase.ViewModel<State> {
    }
}