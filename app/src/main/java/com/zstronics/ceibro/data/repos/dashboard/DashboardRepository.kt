package com.zstronics.ceibro.data.repos.dashboard

import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.base.BaseNetworkRepository
import com.zstronics.ceibro.data.repos.auth.signup.GenericResponse
import com.zstronics.ceibro.data.repos.dashboard.connections.AllConnectionsResponse
import com.zstronics.ceibro.data.repos.dashboard.invites.MyInvitations
import com.zstronics.ceibro.data.repos.dashboard.invites.SendInviteRequest
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
        override suspend fun getAllInvites(): ApiResponse<MyInvitations> = executeSafely(
            call =
            {
                service.getAllInvites()
            }
        )
        override suspend fun sendInvite(sendInviteRequest: SendInviteRequest): ApiResponse<GenericResponse> = executeSafely(
            call =
            {
                service.sendInvite(sendInviteRequest)
            }
        )
        override suspend fun acceptOrRejectInvitation(accepted: Boolean, inviteId: String): ApiResponse<GenericResponse> =
            executeSafely(
                call =
                {
                    service.acceptOrRejectInvitation(accepted, inviteId)
                }
            )
        override suspend fun acceptOrRejectAllInvitations(accepted: Boolean): ApiResponse<GenericResponse> =
            executeSafely(
                call =
                {
                    service.acceptOrRejectAllInvitations(accepted)
                }
            )


}