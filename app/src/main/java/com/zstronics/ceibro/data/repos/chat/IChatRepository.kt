package com.zstronics.ceibro.data.repos.chat

import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.repos.chat.messages.*
import com.zstronics.ceibro.data.repos.chat.room.ChatRoomsResponse

interface IChatRepository {
    suspend fun chat(type: String, favorite: Boolean): ApiResponse<ChatRoomsResponse>
    suspend fun messages(roomId: String, limit: Int = 51): ApiResponse<MessagesResponse>
    suspend fun replyOrSendMessage(message: NewMessageRequest): ApiResponse<NewMessageResponse>
    suspend fun addChatRoomToFav(roomId: String): ApiResponse<ChatRoomAddToFavResponse>
    suspend fun postQuestion(questionRequest: QuestionRequest): ApiResponse<NewMessageResponse>
}