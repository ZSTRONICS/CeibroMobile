package com.zstronics.ceibro.data.repos.chat

import com.zstronics.ceibro.data.repos.chat.messages.*
import com.zstronics.ceibro.data.repos.chat.room.ChatRoom
import com.zstronics.ceibro.data.repos.chat.room.ChatRoomsResponse
import com.zstronics.ceibro.data.repos.chat.room.new_chat.IndividualChatResponse
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ChatRepositoryService {
    @POST("chat/rooms/getchat")
    suspend fun chat(
        @Query("type") type: String = "all",
        @Query("favourite") favorite: Boolean = false
    ): Response<ChatRoomsResponse>

    @POST("chat/room/messages/{roomId}")
    suspend fun messages(
        @Path("roomId") roomId: String,
        @Query("limit") limit: Int
    ): Response<MessagesResponse>

    @POST("chat/message/reply")
    suspend fun replyOrSendMessage(@Body message: NewMessageRequest): Response<NewMessageResponse>


    @POST("chat/room/favourite/{roomId}")
    suspend fun addChatRoomToFav(
        @Path("roomId") roomId: String,
    ): Response<ChatRoomAddToFavResponse>

    @POST("chat/message/questioniar")
    suspend fun postQuestion(@Body requestBody : RequestBody): Response<NewMessageResponse>

    @POST("chat/room/single/{userId}")
    suspend fun createIndividualChat(@Path("userId") userId: String): Response<IndividualChatResponse>

}