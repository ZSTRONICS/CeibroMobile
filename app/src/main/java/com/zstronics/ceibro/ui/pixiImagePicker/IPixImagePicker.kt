package com.zstronics.ceibro.ui.pixiImagePicker

import com.zstronics.ceibro.base.interfaces.IBase

interface IPixImagePicker {
    interface State : IBase.State {
    }

    interface ViewModel : IBase.ViewModel<State> {
    }
}