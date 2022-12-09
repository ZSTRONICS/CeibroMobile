package com.zstronics.ceibro.ui.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.repos.chat.IChatRepository
import com.zstronics.ceibro.data.repos.chat.room.ChatRoom
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ChatVM @Inject constructor(
    override val viewState: ChatState, private val chatRepository: IChatRepository
) : HiltBaseViewModel<IChat.State>(), IChat.ViewModel {

    private val _chatRooms: MutableLiveData<MutableList<ChatRoom>> = MutableLiveData()
    val chatRooms: LiveData<MutableList<ChatRoom>> = _chatRooms

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
                    _chatRooms.postValue(data.chatRooms as MutableList<ChatRoom>?)
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
}