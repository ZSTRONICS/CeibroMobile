package com.zstronics.ceibro.data.repos.projects

import com.zstronics.ceibro.data.repos.chat.messages.ChatRoomAddToFavResponse
import com.zstronics.ceibro.data.repos.chat.messages.MessagesResponse
import com.zstronics.ceibro.data.repos.chat.messages.NewMessageRequest
import com.zstronics.ceibro.data.repos.chat.messages.NewMessageResponse
import com.zstronics.ceibro.data.repos.chat.room.ChatRoomsResponse
import com.zstronics.ceibro.data.repos.projects.projectsmain.AllProjectsResponse
import retrofit2.Response
import retrofit2.http.*

interface ProjectRepositoryService {
    @GET("project")
    suspend fun getProjects(@Query("publishStatus") publishStatus: String = "all"): Response<AllProjectsResponse>
}