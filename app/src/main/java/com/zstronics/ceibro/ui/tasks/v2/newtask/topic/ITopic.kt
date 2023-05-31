package com.zstronics.ceibro.ui.tasks.v2.newtask.topic

import com.zstronics.ceibro.base.interfaces.IBase

interface ITopic {
    interface State : IBase.State {
    }

    interface ViewModel : IBase.ViewModel<State> {
    }
}