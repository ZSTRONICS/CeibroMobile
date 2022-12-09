package com.zstronics.ceibro.data.repos.chat

import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.base.BaseNetworkRepository
import com.zstronics.ceibro.data.repos.chat.messages.*
import com.zstronics.ceibro.data.repos.chat.room.ChatRoomsResponse
import com.zstronics.ceibro.data.repos.chat.room.new_chat.IndividualChatResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import javax.inject.Inject


class ChatRepository @Inject constructor(
    private val service: ChatRepositoryService
) : IChatRepository, BaseNetworkRepository() {
    override suspend fun chat(type: String, favorite: Boolean): ApiResponse<ChatRoomsResponse> =
        executeSafely(
            call =
            {
                service.chat(type, favorite)
            }
        )

    override suspend fun messages(roomId: String, limit: Int): ApiResponse<MessagesResponse> =
        executeSafely(
            call =
            {
                service.messages(roomId, limit)
            }
        )

    override suspend fun replyOrSendMessage(message: NewMessageRequest): ApiResponse<NewMessageResponse> =
        executeSafely(
            call =
            {
                service.replyOrSendMessage(message)
            }
        )

    override suspend fun addChatRoomToFav(roomId: String): ApiResponse<ChatRoomAddToFavResponse> =
        executeSafely(
            call =
            {
                service.addChatRoomToFav(roomId)
            }
        )

    override suspend fun postQuestion(questionRequest: QuestionRequest): ApiResponse<NewMessageResponse> =
        executeSafely(
            call =
            {
                val body: RequestBody = RequestBody.create(
                    "application/json".toMediaTypeOrNull(),
                    questionRequest.toString()
                )
                service.postQuestion(body)
            }
        )

    override suspend fun createIndividualChat(userId: String): ApiResponse<IndividualChatResponse> =
        executeSafely(
            call =
            {

                service.createIndividualChat(userId)
            }
        )
}