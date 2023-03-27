package com.zstronics.ceibro.ui.admin.admins

import com.zstronics.ceibro.base.interfaces.IBase

interface IAdmins {
    interface State : IBase.State {
    }

    interface ViewModel : IBase.ViewModel<State> {
    }
}