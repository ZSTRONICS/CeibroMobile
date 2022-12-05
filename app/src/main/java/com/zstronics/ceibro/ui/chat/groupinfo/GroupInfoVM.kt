package com.zstronics.ceibro.ui.chat.groupinfo

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.repos.chat.room.ChatRoom
import com.zstronics.ceibro.data.repos.chat.room.Member
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.ui.chat.extensions.getChatTitle
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class GroupInfoVM @Inject constructor(
    override val viewState: GroupInfoState,
    val sessionManager: SessionManager,
) : HiltBaseViewModel<IGroupInfo.State>(), IGroupInfo.ViewModel {


    override fun fetchExtras(extras: Bundle?) {
        val user = sessionManager.getUser().value
        super.fetchExtras(extras)
        extras?.let {
            val chatRoom: ChatRoom? = it.getParcelable("chatRoom")
            with(viewState) {
                chatTitle.value = chatRoom?.getChatTitle(user)
                isGroupChat.value = chatRoom?.isGroupChat
                project.value = chatRoom?.project
            }
        }
    }
}