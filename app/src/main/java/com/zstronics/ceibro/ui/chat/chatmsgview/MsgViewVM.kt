package com.zstronics.ceibro.ui.chat.chatmsgview

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.repos.chat.IChatRepository
import com.zstronics.ceibro.data.repos.chat.messages.MediaMessage
import com.zstronics.ceibro.data.repos.chat.messages.MessagesResponse
import com.zstronics.ceibro.data.repos.chat.messages.NewMessageRequest
import com.zstronics.ceibro.data.repos.chat.messages.SocketMessageRequest
import com.zstronics.ceibro.data.repos.chat.room.ChatRoom
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.ui.chat.adapter.MessagesAdapter
import com.zstronics.ceibro.ui.chat.extensions.getChatTitle
import com.zstronics.ceibro.ui.enums.EventType
import com.zstronics.ceibro.ui.enums.MessageType
import com.zstronics.ceibro.ui.socket.SocketHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject

@HiltViewModel
class MsgViewVM @Inject constructor(
    override val viewState: MsgViewState,
    val sessionManager: SessionManager,
    private val chatRepository: IChatRepository
) : HiltBaseViewModel<IMsgView.State>(), IMsgView.ViewModel {
    val user = sessionManager.getUser().value
    val userId = user?.id

    val sender: MessagesResponse.ChatMessage.Sender
        get() = MessagesResponse.ChatMessage.Sender(
            firstName = user?.firstName ?: "",
            surName = user?.surName ?: "",
            id = userId ?: ""
        )
    private val _chatMessages: MutableLiveData<MutableList<MessagesResponse.ChatMessage>> =
        MutableLiveData()
    val chatMessages: MutableLiveData<MutableList<MessagesResponse.ChatMessage>> = _chatMessages
    var chatRoom: ChatRoom? = null

    @Inject
    lateinit var adapter: MessagesAdapter

    override fun onFirsTimeUiCreate(bundle: Bundle?) {
        super.onFirsTimeUiCreate(bundle)
        val user = sessionManager.getUser().value
        with(viewState) {
            chatRoom = bundle?.getParcelable("chatRoom")
            chatTitle.value = chatRoom?.getChatTitle(user)
            isGroupChat.value = chatRoom?.isGroupChat
            if (chatRoom?.isGroupChat == true) {
                project.value = chatRoom?.project
            }
            chatRoom?.id?.let { loadMessages(it) }
        }
    }

    override fun loadMessages(roomId: String) {
        launch {
            loading(true)
            when (val response = chatRepository.messages(roomId)) {

                is ApiResponse.Success -> {
                    loading(false)
                    val data = response.data
                    _chatMessages.postValue(data.messages.toMutableList())
                }

                is ApiResponse.Error -> {
                    loading(false)
                }
            }
        }
    }

    override fun replyOrSendMessage(
        message: NewMessageRequest
    ) {

        launch {
            when (val response = chatRepository.replyOrSendMessage(message)) {

                is ApiResponse.Success -> {
                    val data = response.data
                }

                is ApiResponse.Error -> {
                }
            }
        }
    }

    fun sendMessage(
        message: String? = null,
        chatId: String?,
        eventType: EventType = EventType.SEND_MESSAGE,
        messageType: MessageType = MessageType.MESSAGE,
        file: File? = null
    ) {

        val from = sessionManager.getUser().value?.id

        val files = arrayListOf<MediaMessage?>()
        if (file != null) {
            files.add(MediaMessage(file.extension, file.name, file.absolutePath.toString()))
        }

        val newMessage = NewMessageRequest(
            message = message,
            chat = chatId,
            type = messageType.name.lowercase(),
            messageId = null,
            files = files,
            myId = System.currentTimeMillis().toString()
        )

        if (viewState.isQuotedMessage.value == true) {
            newMessage.messageId = viewState.quotedMessage.value?.id
        }
        val gson = Gson()
        val jsonData = gson.toJson(newMessage)


        val messageRequest = SocketMessageRequest(
            userId = userId,
            eventType = eventType.name,
            data = jsonData)
        val json = gson.toJson(messageRequest)

        SocketHandler.sendRequest(json)
//        replyOrSendMessage(newMessage)
    }

    override fun composeMessageToSend(
        message: String?,
        scrollToPosition: ((lastPosition: Int) -> Unit?)?
    ) {
        val chatId: String? = chatRoom?.id

        sendMessage(message, chatId)

        /* Dump new message locally*/

        val replyTo = if (viewState.isQuotedMessage.value == true) {
            viewState.quotedMessage.value
        } else null

        val messageRes: MessagesResponse.ChatMessage =
            composeLocalMessage(replyTo = replyTo, message = message)

        adapter.appendMessage(messageRes) { lastPosition ->
            scrollToPosition?.invoke(lastPosition)
        }

        hideQuoted()
        viewState.messageBoxBody.value = ""
    }

    fun hideQuoted() {
        viewState.isQuotedMessage.value = false
    }

    fun showQuoted() {
        viewState.isQuotedMessage.value = true
    }

    fun composeLocalMessage(
        replyTo: MessagesResponse.ChatMessage? = null,
        message: String?,
        messageType: MessageType = MessageType.MESSAGE,
    ): MessagesResponse.ChatMessage {
        val localMessage: MessagesResponse.ChatMessage = MessagesResponse.ChatMessage(
            companyName = user?.companyName ?: "",
            sender = sender,
            time = "Now",
            type = messageType.name.lowercase(),
            myMessage = true,
            message = message ?: "",
            chat = chatRoom?.id
        )
        with(localMessage) {
            replyTo?.let {
                val replyOf: MessagesResponse.ReplyOf =
                    MessagesResponse.ReplyOf(
                        message = replyTo.message,
                        id = replyTo.id,
                        sender = userId ?: "",
                        type = messageType.name.lowercase(),
                        chat = chatRoom?.id
                    )
                this.replyOf = replyOf
            }
        }

        return localMessage
    }

    fun composeFileMessageToSend(
        file: File,
        scrollToPosition: ((lastPosition: Int) -> Unit?)? = null
    ) {
        val chatId: String? = chatRoom?.id

        sendMessage(chatId = chatId, file = file, messageType = MessageType.FILE)

        /* Dump new message locally*/

        val replyTo = if (viewState.isQuotedMessage.value == true) {
            viewState.quotedMessage.value
        } else null

//        val messageRes: MessagesResponse.ChatMessage =
//            composeLocalMessage(replyTo = replyTo, message = message)
//
//        adapter.appendMessage(messageRes) { lastPosition ->
//            scrollToPosition?.invoke(lastPosition)
//        }
//
//        hideQuoted()
//
//        viewState.messageBoxBody.value = ""
    }
}