package com.zstronics.ceibro.ui.forgotpassword

import com.zstronics.ceibro.base.interfaces.IBase

interface IForgotPassword {
    interface State : IBase.State {
    }

    interface ViewModel : IBase.ViewModel<State> {
    }
}