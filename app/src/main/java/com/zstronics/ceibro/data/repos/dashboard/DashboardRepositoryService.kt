package com.zstronics.ceibro.data.repos.dashboard

import com.zstronics.ceibro.data.repos.auth.signup.GenericResponse
import com.zstronics.ceibro.data.repos.dashboard.admins.AdminUsersResponse
import com.zstronics.ceibro.data.repos.dashboard.attachment.GetAllFilesResponse
import com.zstronics.ceibro.data.repos.dashboard.attachment.UploadFilesResponse
import com.zstronics.ceibro.data.repos.dashboard.connections.AllConnectionsResponse
import com.zstronics.ceibro.data.repos.dashboard.connections.CountResponse
import com.zstronics.ceibro.data.repos.dashboard.invites.MyInvitations
import com.zstronics.ceibro.data.repos.dashboard.invites.SendInviteRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface DashboardRepositoryService {
    @GET("users/connections")
    suspend fun getAllConnections(): Response<AllConnectionsResponse>
    @GET("users/connections/count")
    suspend fun getConnectionCount(): Response<CountResponse>

    @GET("users/invite")
    suspend fun getAllInvites(): Response<MyInvitations>

    @POST("users/invite")
    suspend fun sendInvite(@Body sendInviteRequest: SendInviteRequest): Response<GenericResponse>

    @POST("users/invite/accept/{accepted}/{inviteId}")
    suspend fun acceptOrRejectInvitation(
        @Path("accepted") accepted: Boolean,
        @Path("inviteId") inviteId: String
    ): Response<GenericResponse>

    @POST("users/invite/accept-all/{accepted}")
    suspend fun acceptOrRejectAllInvitations(
        @Path("accepted") accepted: Boolean
    ): Response<GenericResponse>

    @Multipart
    @POST("docs/uploadFiles")
    suspend fun uploadFiles(
        @Part("moduleName") moduleName: RequestBody,
        @Part("_id") id: RequestBody,
        @Part files: List<MultipartBody.Part>?
    ): Response<UploadFilesResponse>

    @GET("docs/viewFiles/{module}/{moduleId}")
    suspend fun getFilesByModuleId(
        @Path("module") module: String,
        @Path("moduleId") moduleId: String
    ): Response<GetAllFilesResponse>

    @GET("users")
    suspend fun getAdminsOrUsersList(
        @Query("role") role: String
    ): Response<AdminUsersResponse>
}