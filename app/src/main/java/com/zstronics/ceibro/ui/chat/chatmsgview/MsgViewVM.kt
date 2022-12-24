package com.zstronics.ceibro.ui.chat.chatmsgview

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.repos.chat.IChatRepository
import com.zstronics.ceibro.data.repos.chat.messages.*
import com.zstronics.ceibro.data.repos.chat.messages.socket.AllMessageSeenSocketResponse
import com.zstronics.ceibro.data.repos.chat.messages.socket.MessageSeenSocketResponse
import com.zstronics.ceibro.data.repos.chat.room.ChatRoom
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.ui.chat.adapter.MessagesAdapter
import com.zstronics.ceibro.ui.chat.extensions.getChatTitle
import com.zstronics.ceibro.ui.enums.EventType
import com.zstronics.ceibro.ui.enums.MessageType
import com.zstronics.ceibro.ui.socket.SocketHandler
import com.zstronics.ceibro.utils.DateUtils
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
            id = userId ?: "",
            profilePic = user?.profilePic ?: "",
            companyName = user?.companyName ?: ""
        )
    private val _chatMessages: MutableLiveData<MutableList<MessagesResponse.ChatMessage>> =
        MutableLiveData()
    val chatMessages: MutableLiveData<MutableList<MessagesResponse.ChatMessage>> = _chatMessages
    var chatRoom: ChatRoom? = null

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
        val readAllMessagesJson = readAllMessagesJson(roomId)
        sendMessageStatus(readAllMessagesJson)
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

    private fun readAllMessagesJson(roomId: String): String {
        return Gson().toJson(
            AllMessageSeenSocketResponse(
                data = AllMessageSeenSocketResponse.Data(roomId, userId ?: ""),
                eventType = EventType.ALL_MESSAGE_SEEN.name
            )
        )
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

        //  Creating following json to send message
//        {
//            eventType: SEND_MESSAGE,
//            data: {
//              userId: user.id,
//              message: JSON.stringify(payload),
//            }
//        }

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
        val newMessageJson = gson.toJson(newMessage)


        val messageData = MessageDataRequest(
            userId = userId,
            message = newMessageJson
        )
        val messageDataJson = gson.toJson(messageData)


        val messageRequest = SocketMessageRequest(
            eventType = eventType.name,
            data = messageDataJson
        )
        val json = gson.toJson(messageRequest)

        SocketHandler.sendRequest(json)
//        replyOrSendMessage(newMessage)
    }

    private fun sendMessageStatus(
        json: String
    ) {
        SocketHandler.sendRequest(json)
    }

    override fun composeAndSendMessage(
        message: String?,
        adapter: MessagesAdapter,
        scrollToPosition: ((lastPosition: Int) -> Unit?)?
    ) {
        sendMessage(message, chatRoom?.id)

        val replyTo =
            viewState.quotedMessage.value?.takeIf { viewState.isQuotedMessage.value == true }
        val messageRes = composeLocalMessage(replyTo, message)
        adapter.appendMessage(messageRes) { lastPosition ->
            scrollToPosition?.invoke(lastPosition)
        }
        hideQuoted()
        viewState.messageBoxBody.value = ""
        appendMessageInMessagesList(messageRes)
    }

    fun appendMessageInMessagesList(messageRes: MessagesResponse.ChatMessage) {
        val chatMessages = _chatMessages.value
        chatMessages?.add(messageRes)
        _chatMessages.value = chatMessages
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
            sender = sender,
            createdAt = DateUtils.getCurrentTimeStamp(),
            type = messageType.name.lowercase(),
            message = message ?: "",
            chat = chatRoom?.id
        )
        with(localMessage) {
            replyTo?.let {
                val replyOf: MessagesResponse.ReplyOf =
                    MessagesResponse.ReplyOf(
                        message = replyTo.message,
                        replySender = MessagesResponse.ReplyOf.ReplySender(
                            firstName = replyTo.sender.firstName,
                            surName = replyTo.sender.surName,
                            id = replyTo.sender.id,
                            profilePic = replyTo.sender.profilePic,
                            companyName = replyTo.sender.companyName
                        ),
                        id = replyTo.id,
                        type = messageType.name.lowercase(),
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

    fun readMessage(messageId: String, roomId: String?, eventType: EventType) {
        val data = MessageStatusRequest(
            userId = userId,
            roomId = roomId,
            messageId = messageId
        )
        val messageRequest = SocketMessageRequest(
            eventType = eventType.name,
            data = Gson().toJson(data)
        )
        sendMessageStatus(Gson().toJson(messageRequest))
    }

    fun updateOtherLastMessageSeen(messageSeen: MessageSeenSocketResponse) {

        if (messageSeen.data.roomId == chatRoom?.id &&
            (messageSeen.data.userId == userId).not()
        ) {
            // update last message

            val chatMessages = _chatMessages.value
            chatMessages?.findLast { it.sender.id == userId }?.readBy =
                messageSeen.data.updatedMessage[0].readBy
            _chatMessages.postValue(chatMessages)
        }
    }
}