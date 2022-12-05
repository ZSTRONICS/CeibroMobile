package com.zstronics.ceibro.ui.chat.messageinfo

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.state.BaseState
import com.zstronics.ceibro.data.repos.chat.messages.MessagesResponse
import javax.inject.Inject

class MessageInfoState @Inject constructor() : BaseState(), IMessageInfo.State {
    override val message: MutableLiveData<MessagesResponse.ChatMessage> = MutableLiveData()
}