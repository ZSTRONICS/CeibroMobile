package com.zstronics.ceibro.ui.chat.chat_members

import com.zstronics.ceibro.base.interfaces.IBase

interface IChatMembers {
    interface State : IBase.State {
    }

    interface ViewModel : IBase.ViewModel<State> {
    }
}