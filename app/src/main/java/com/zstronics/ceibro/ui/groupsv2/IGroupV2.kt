package com.zstronics.ceibro.ui.groupsv2

import com.zstronics.ceibro.base.interfaces.IBase

interface IGroupV2 {
    interface State : IBase.State {
    }

    interface ViewModel : IBase.ViewModel<State> {
    }
}