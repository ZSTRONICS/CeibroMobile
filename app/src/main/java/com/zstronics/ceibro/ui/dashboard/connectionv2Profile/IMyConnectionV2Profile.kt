package com.zstronics.ceibro.ui.dashboard.connectionv2Profile

import com.zstronics.ceibro.base.interfaces.IBase

interface IMyConnectionV2Profile {
    interface State : IBase.State {
    }

    interface ViewModel : IBase.ViewModel<State> {
    }
}