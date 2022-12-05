package com.zstronics.ceibro.ui.chat.newchat

import com.zstronics.ceibro.base.interfaces.IBase

interface INewChat {
    interface State : IBase.State {
    }

    interface ViewModel : IBase.ViewModel<State> {
    }
}