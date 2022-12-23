package com.zstronics.ceibro.data.repos.dashboard

import com.zstronics.ceibro.data.repos.auth.login.LoginRequest
import com.zstronics.ceibro.data.repos.auth.signup.GenericResponse
import com.zstronics.ceibro.data.repos.dashboard.connections.AllConnectionsResponse
import com.zstronics.ceibro.data.repos.dashboard.invites.MyInvitations
import com.zstronics.ceibro.data.repos.dashboard.invites.SendInviteRequest
import retrofit2.Response
import retrofit2.http.*

interface DashboardRepositoryService {
    @GET("users/connections")
    suspend fun getAllConnections(): Response<AllConnectionsResponse>
    @GET("users/invite")
    suspend fun getAllInvites(): Response<MyInvitations>
    @POST("users/invite")
    suspend fun sendInvite(@Body sendInviteRequest: SendInviteRequest): Response<GenericResponse>
    @POST("users/invite/accept/{accepted}/{inviteId}")
    suspend fun acceptOrRejectInvitation(
        @Path("accepted") accepted: Boolean,
        @Path("inviteId") inviteId: String
    ): Response<GenericResponse>
}