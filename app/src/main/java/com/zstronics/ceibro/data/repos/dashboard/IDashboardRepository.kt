package com.zstronics.ceibro.data.repos.dashboard

import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.repos.dashboard.connections.AllConnectionsResponse
import com.zstronics.ceibro.data.repos.dashboard.invites.MyInvitations

interface IDashboardRepository {
    suspend fun getAllConnections(): ApiResponse<AllConnectionsResponse>
    suspend fun getAllInvites(): ApiResponse<MyInvitations>
}