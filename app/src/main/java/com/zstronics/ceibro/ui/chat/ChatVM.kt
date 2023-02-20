package com.zstronics.ceibro.ui.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.repos.chat.IChatRepository
import com.zstronics.ceibro.data.repos.chat.messages.SocketReceiveMessageResponse
import com.zstronics.ceibro.data.repos.chat.room.ChatRoom
import com.zstronics.ceibro.data.repos.chat.room.LastMessage
import com.zstronics.ceibro.data.sessions.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ChatVM @Inject constructor(
    override val viewState: ChatState,
    private val chatRepository: IChatRepository,
    val sessionManager: SessionManager,
) : HiltBaseViewModel<IChat.State>(), IChat.ViewModel {

    val user = sessionManager.getUser().value
    val userId = user?.id

    private val _chatRooms: MutableLiveData<MutableList<ChatRoom>?> = MutableLiveData()
    val chatRooms: LiveData<MutableList<ChatRoom>?> = _chatRooms

    override fun onResume() {
        super.onResume()
        loadChat("all", false)
    }

    override fun loadChat(type: String, favorite: Boolean) {
        launch {
            loading(true)
            when (val response = chatRepository.chat(type, favorite)) {

                is ApiResponse.Success -> {
                    loading(false)
                    val data = response.data
                    val chatRooms = data.chatRooms as MutableList<ChatRoom>?
                    chatRooms?.sortByDescending { it.unreadCount }
                    _chatRooms.postValue(chatRooms)
                }

                is ApiResponse.Error -> {
                    loading(false)
                }
            }
        }
    }

    override fun addChatToFav(roomId: String) {
        launch {
            when (val response = chatRepository.addChatRoomToFav(roomId)) {

                is ApiResponse.Success -> {
                    loading(false)
                    val data = response.data
                    alert(data.message)
                }

                is ApiResponse.Error -> {
                    loading(false)
                }
            }
        }
    }

    override fun deleteConversation(roomId: String, position: Int) {
        loading(true)
        launch {
            when (val response = chatRepository.deleteConversation(roomId)) {
                is ApiResponse.Success -> {
                    loading(false, response.data.message)
                    val chatRoomTemp = _chatRooms.value
                    chatRoomTemp?.removeAt(position)
                    _chatRooms.value = chatRoomTemp
                }

                is ApiResponse.Error -> {
                    loading(false)
                }
            }
        }
    }

    fun updateChatListOnNewMessageReceived(newMessage: SocketReceiveMessageResponse) {
        when {
            isMessageExist(newMessage) -> {
                val chatRooms = _chatRooms.value
                val newMessageChat = chatRooms?.find { it.id == newMessage.data.messageData.chat }
                val newMessageChatIndex = chatRooms?.indexOf(newMessageChat) ?: -1
                newMessageChat?.apply {
                    this.unreadCount += 1
                    this.lastMessage = LastMessage(
                        newMessage.data.messageData.message.id,
                        newMessage.data.messageData.message.message
                    )
                }

                newMessageChat?.let { chatRooms.set(newMessageChatIndex, it) }
                chatRooms?.sortByDescending { it.unreadCount }
                _chatRooms.postValue(chatRooms)
            }
            else -> {
                /// TODO("Add new message to list but the data is being received in the socket is different than the data is already populated in the list.")
                loadChat("all", false)
            }
        }
    }

    private fun isMessageExist(newMessage: SocketReceiveMessageResponse): Boolean {
        return chatRooms.value?.find { it.id == newMessage.data.messageData.chat } != null
    }

}