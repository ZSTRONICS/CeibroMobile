package com.zstronics.ceibro.ui.tasks.subtaskeditmembers

import com.zstronics.ceibro.base.interfaces.IBase

interface ISubTaskEditMembers {
    interface State : IBase.State {
    }

    interface ViewModel : IBase.ViewModel<State> {
    }
}