package com.zstronics.ceibro.ui.invitations

import com.zstronics.ceibro.base.interfaces.IBase

interface IInvitations {
    interface State : IBase.State {
    }

    interface ViewModel : IBase.ViewModel<State> {
        fun loadInvitations()
    }
}