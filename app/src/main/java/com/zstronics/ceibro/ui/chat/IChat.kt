package com.zstronics.ceibro.ui.chat

import com.zstronics.ceibro.base.interfaces.IBase

interface IChat {
    interface State : IBase.State {
    }

    interface ViewModel : IBase.ViewModel<State> {
        fun loadChat(type: String, favorite: Boolean)
        fun addChatToFav(roomId: String)
    }
}