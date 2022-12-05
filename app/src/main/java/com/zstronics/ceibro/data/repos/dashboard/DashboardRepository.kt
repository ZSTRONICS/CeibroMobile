package com.zstronics.ceibro.data.repos.dashboard

import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.base.BaseNetworkRepository
import com.zstronics.ceibro.data.repos.dashboard.connections.AllConnectionsResponse
import javax.inject.Inject

class DashboardRepository @Inject constructor(
    private val service: DashboardRepositoryService
) : IDashboardRepository, BaseNetworkRepository() {
        override suspend fun getAllConnections(): ApiResponse<AllConnectionsResponse> = executeSafely(
        call =
        {
            service.getAllConnections()
        }
    )


}