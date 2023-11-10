package com.zstronics.ceibro.ui.projectv2.projectdetailv2

import com.zstronics.ceibro.base.interfaces.IBase

interface IProjectDetailV2 {
    interface State : IBase.State {
    }

    interface ViewModel : IBase.ViewModel<State> {
    }
}