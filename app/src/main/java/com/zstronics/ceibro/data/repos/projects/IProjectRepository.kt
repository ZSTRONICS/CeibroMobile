package com.zstronics.ceibro.data.repos.projects

import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.repos.chat.messages.ChatRoomAddToFavResponse
import com.zstronics.ceibro.data.repos.chat.messages.MessagesResponse
import com.zstronics.ceibro.data.repos.chat.messages.NewMessageRequest
import com.zstronics.ceibro.data.repos.chat.messages.NewMessageResponse
import com.zstronics.ceibro.data.repos.chat.room.ChatRoomsResponse
import com.zstronics.ceibro.data.repos.projects.projectsmain.AllProjectsResponse

interface IProjectRepository {
    suspend fun getProjects(publishStatus:String): ApiResponse<AllProjectsResponse>
}