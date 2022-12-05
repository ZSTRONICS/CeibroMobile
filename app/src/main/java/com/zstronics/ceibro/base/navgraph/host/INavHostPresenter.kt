package com.zstronics.ceibro.base.navgraph.host

import com.zstronics.ceibro.base.interfaces.IBase

interface INavHostPresenter {
    interface View : IBase.View<ViewModel>
    interface ViewModel : IBase.ViewModel<State>

    interface State : IBase.State
}