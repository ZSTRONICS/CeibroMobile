package com.zstronics.ceibro.ui.chat.media

import com.zstronics.ceibro.base.interfaces.IBase

interface IMedia {
    interface State : IBase.State {
    }

    interface ViewModel : IBase.ViewModel<State> {
    }
}