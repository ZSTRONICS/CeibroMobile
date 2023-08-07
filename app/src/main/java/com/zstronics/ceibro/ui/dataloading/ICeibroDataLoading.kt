package com.zstronics.ceibro.ui.dataloading

import com.zstronics.ceibro.base.interfaces.IBase

interface ICeibroDataLoading {
    interface State : IBase.State {
    }

    interface ViewModel : IBase.ViewModel<State> {
    }
}