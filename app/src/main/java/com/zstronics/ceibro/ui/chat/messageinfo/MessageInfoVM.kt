package com.zstronics.ceibro.ui.chat.messageinfo

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.repos.chat.messages.MessagesResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MessageInfoVM @Inject constructor(
    override val viewState: MessageInfoState,
) : HiltBaseViewModel<IMessageInfo.State>(), IMessageInfo.ViewModel {
    private val _readBy: MutableLiveData<List<MessagesResponse.ChatMessage.ReadBy>> =
        MutableLiveData()
    val readBy: LiveData<List<MessagesResponse.ChatMessage.ReadBy>> = _readBy
    override fun fetchExtras(extras: Bundle?) {
        super.fetchExtras(extras)
        extras?.let {
            val message: MessagesResponse.ChatMessage? = it.getParcelable("message")
            viewState.message.value = message
            _readBy.value = message?.readBy
        }
    }
}