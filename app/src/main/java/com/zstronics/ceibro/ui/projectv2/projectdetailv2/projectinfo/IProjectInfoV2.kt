package com.zstronics.ceibro.ui.projectv2.projectdetailv2.projectinfo

import com.zstronics.ceibro.base.interfaces.IBase

interface IProjectInfoV2 {
    interface State : IBase.State {
    }

    interface ViewModel : IBase.ViewModel<State> {
    }
}