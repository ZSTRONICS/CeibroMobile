package com.zstronics.ceibro.ui.chat.chatmsgview

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.interfaces.IBase
import com.zstronics.ceibro.data.repos.chat.messages.MessagesResponse
import com.zstronics.ceibro.data.repos.chat.messages.NewMessageRequest
import com.zstronics.ceibro.data.repos.chat.room.Project
import com.zstronics.ceibro.ui.chat.adapter.MessagesAdapter

interface IMsgView {
    interface State : IBase.State {
        val chatTitle: MutableLiveData<String>
        val project: MutableLiveData<Project>
        val isGroupChat: MutableLiveData<Boolean>
        val recordingVoice: MutableLiveData<Boolean>
        val messageBoxBody: MutableLiveData<String>
        var quotedMessage: MutableLiveData<MessagesResponse.ChatMessage>
        val isQuotedMessage: MutableLiveData<Boolean>
    }

    interface ViewModel : IBase.ViewModel<State> {
        fun loadMessages(roomId: String)
        fun replyOrSendMessage(
            message: NewMessageRequest
        )
        fun composeAndSendMessage(
            message: String?,
            adapter: MessagesAdapter,
            scrollToPosition: ((lastPosition: Int) -> Unit?)? = null
        )
    }
}