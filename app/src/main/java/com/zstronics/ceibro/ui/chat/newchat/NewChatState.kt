package com.zstronics.ceibro.ui.chat.newchat

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.state.BaseState
import javax.inject.Inject

class NewChatState @Inject constructor() : BaseState(), INewChat.State {
    override val name: MutableLiveData<String> = MutableLiveData()
}