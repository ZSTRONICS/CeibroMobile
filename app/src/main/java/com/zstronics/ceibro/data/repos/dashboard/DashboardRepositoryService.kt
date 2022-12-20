package com.zstronics.ceibro.data.repos.dashboard

import com.zstronics.ceibro.data.repos.dashboard.connections.AllConnectionsResponse
import com.zstronics.ceibro.data.repos.dashboard.invites.MyInvitations
import retrofit2.Response
import retrofit2.http.*

interface DashboardRepositoryService {
    @GET("users/connections")
    suspend fun getAllConnections(): Response<AllConnectionsResponse>
    @GET("users/invite")
    suspend fun getAllInvites(): Response<MyInvitations>
}