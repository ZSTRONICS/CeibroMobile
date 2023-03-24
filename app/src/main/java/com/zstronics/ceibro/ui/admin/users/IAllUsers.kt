package com.zstronics.ceibro.ui.admin.users

import com.zstronics.ceibro.base.interfaces.IBase

interface IAllUsers {
    interface State : IBase.State {
    }

    interface ViewModel : IBase.ViewModel<State> {
    }
}