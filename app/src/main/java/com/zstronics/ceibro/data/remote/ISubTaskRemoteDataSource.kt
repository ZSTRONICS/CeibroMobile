package com.zstronics.ceibro.data.remote

import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.repos.task.models.*

interface ISubTaskRemoteDataSource {
    suspend fun getAllSubTasksForUser(state: String = "all"): ApiResponse<AllSubtasksResponse>
}