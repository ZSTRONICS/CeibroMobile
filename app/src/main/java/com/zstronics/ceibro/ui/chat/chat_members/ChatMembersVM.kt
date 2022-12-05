package com.zstronics.ceibro.ui.chat.chat_members

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.repos.chat.room.ChatRoom
import com.zstronics.ceibro.data.repos.chat.room.Member
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ChatMembersVM @Inject constructor(
    override val viewState: ChatMembersState
) : HiltBaseViewModel<IChatMembers.State>(), IChatMembers.ViewModel {

    private val _chatMembers: MutableLiveData<MutableList<Member>> = MutableLiveData()
    val chatMembers: LiveData<MutableList<Member>> = _chatMembers
    override fun fetchExtras(extras: Bundle?) {
        super.fetchExtras(extras)
        extras?.let {
            val chatRoom: ChatRoom? = it.getParcelable("chatRoom")
            _chatMembers.postValue(chatRoom?.members)
        }
    }
}