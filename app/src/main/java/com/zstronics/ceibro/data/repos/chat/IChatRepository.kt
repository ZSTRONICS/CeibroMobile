package com.zstronics.ceibro.data.repos.chat

import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.repos.chat.messages.*
import com.zstronics.ceibro.data.repos.chat.room.ChatRoomsResponse
import com.zstronics.ceibro.data.repos.chat.room.new_chat.GroupChatResponse
import com.zstronics.ceibro.data.repos.chat.room.new_chat.IndividualChatResponse

interface IChatRepository {
    suspend fun chat(type: String, favorite: Boolean): ApiResponse<ChatRoomsResponse>
    suspend fun messages(roomId: String, limit: Int = 20): ApiResponse<MessagesResponse>
    suspend fun replyOrSendMessage(message: NewMessageRequest): ApiResponse<NewMessageResponse>
    suspend fun addChatRoomToFav(roomId: String): ApiResponse<ChatRoomAddToFavResponse>
    suspend fun postQuestion(questionRequest: QuestionRequest): ApiResponse<NewMessageResponse>
    suspend fun createIndividualChat(userId: String): ApiResponse<GroupChatResponse>
    suspend fun deleteConversation(roomId: String): ApiResponse<ChatRoomAddToFavResponse>
    suspend fun createGroupChat(request: NewGroupChatRequest): ApiResponse<GroupChatResponse>
    suspend fun fetchMoreMessages(
        roomId: String,
        skip: Int,
        limit: Int
    ): ApiResponse<MessagesResponse>
}