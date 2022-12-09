package com.zstronics.ceibro.ui.chat.individualchat

import com.zstronics.ceibro.base.interfaces.IBase

interface ISingleNewChat {
    interface State : IBase.State {
    }

    interface ViewModel : IBase.ViewModel<State> {
        fun loadConnections()
        fun createIndividualChat()
    }
}