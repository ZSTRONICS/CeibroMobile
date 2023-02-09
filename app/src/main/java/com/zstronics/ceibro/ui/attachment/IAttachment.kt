package com.zstronics.ceibro.ui.attachment

import com.zstronics.ceibro.base.interfaces.IBase

interface IAttachment {
    interface State : IBase.State {
    }

    interface ViewModel : IBase.ViewModel<State> {
    }
}