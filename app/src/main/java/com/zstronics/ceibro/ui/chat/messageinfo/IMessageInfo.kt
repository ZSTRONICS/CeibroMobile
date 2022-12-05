package com.zstronics.ceibro.ui.chat.messageinfo

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.interfaces.IBase
import com.zstronics.ceibro.data.repos.chat.messages.MessagesResponse

interface IMessageInfo {
    interface State : IBase.State {
        val message: MutableLiveData<MessagesResponse.ChatMessage>
    }

    interface ViewModel : IBase.ViewModel<State> {
    }
}