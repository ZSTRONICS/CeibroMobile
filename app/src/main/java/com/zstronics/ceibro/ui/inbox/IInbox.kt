package com.zstronics.ceibro.ui.inbox

import com.zstronics.ceibro.base.interfaces.IBase

interface IInbox {
    interface State : IBase.State {
    }

    interface ViewModel : IBase.ViewModel<State> {
    }
}