package com.zstronics.ceibro.ui.chat.chatmsgview

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.state.BaseState
import com.zstronics.ceibro.data.repos.chat.messages.MessagesResponse
import com.zstronics.ceibro.data.repos.chat.room.Project
import javax.inject.Inject

class MsgViewState @Inject constructor() : BaseState(), IMsgView.State {
    override val chatTitle: MutableLiveData<String> = MutableLiveData()
    override val project: MutableLiveData<Project> = MutableLiveData(Project("", ""))
    override val isGroupChat: MutableLiveData<Boolean> = MutableLiveData()
    override val recordingVoice: MutableLiveData<Boolean> = MutableLiveData(false)
    override val messageBoxBody: MutableLiveData<String> = MutableLiveData("")
    override var quotedMessage: MutableLiveData<MessagesResponse.ChatMessage> = MutableLiveData()
    override val isQuotedMessage: MutableLiveData<Boolean> = MutableLiveData(false)
}