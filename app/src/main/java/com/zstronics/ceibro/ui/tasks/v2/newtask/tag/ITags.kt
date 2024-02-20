package com.zstronics.ceibro.ui.tasks.v2.newtask.tag

import com.zstronics.ceibro.base.interfaces.IBase

interface ITags {
    interface State : IBase.State {
    }

    interface ViewModel : IBase.ViewModel<State> {
    }
}