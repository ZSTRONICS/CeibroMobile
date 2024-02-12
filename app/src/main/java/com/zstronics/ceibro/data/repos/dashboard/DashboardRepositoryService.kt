package com.zstronics.ceibro.data.repos.dashboard

import com.zstronics.ceibro.data.repos.auth.signup.GenericResponse
import com.zstronics.ceibro.data.repos.dashboard.admins.AdminUsersResponse
import com.zstronics.ceibro.data.repos.dashboard.attachment.GetAllFilesResponse
import com.zstronics.ceibro.data.repos.dashboard.attachment.UploadFilesResponse
import com.zstronics.ceibro.data.repos.dashboard.attachment.v2.UploadFilesV2Response
import com.zstronics.ceibro.data.repos.dashboard.connections.AllConnectionsResponse
import com.zstronics.ceibro.data.repos.dashboard.connections.CountResponse
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.AllCeibroConnections
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.CeibroConnectionGroupV2
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.GetConnectionGroupsResponse
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.NewConnectionGroupRequest
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.RecentCeibroConnections
import com.zstronics.ceibro.data.repos.dashboard.contacts.BlockUserResponse
import com.zstronics.ceibro.data.repos.dashboard.contacts.ContactSyncEnableResponse
import com.zstronics.ceibro.data.repos.dashboard.contacts.GetContactsResponse
import com.zstronics.ceibro.data.repos.dashboard.contacts.SyncContactsRequest
import com.zstronics.ceibro.data.repos.dashboard.invites.MyInvitations
import com.zstronics.ceibro.data.repos.dashboard.invites.SendInviteRequest
import com.zstronics.ceibro.data.repos.task.models.v2.EventV2Response
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface DashboardRepositoryService {
    @GET("v1/users/connections")
    suspend fun getAllConnections(): Response<AllConnectionsResponse>

    @GET("v1/users/connections/count")
    suspend fun getConnectionCount(): Response<CountResponse>

    @GET("v1/users/invite")
    suspend fun getAllInvites(): Response<MyInvitations>

    @POST("v1/users/invite")
    suspend fun sendInvite(@Body sendInviteRequest: SendInviteRequest): Response<GenericResponse>

    @POST("v1/users/invite/accept/{accepted}/{inviteId}")
    suspend fun acceptOrRejectInvitation(
        @Path("accepted") accepted: Boolean,
        @Path("inviteId") inviteId: String
    ): Response<GenericResponse>

    @POST("v1/users/invite/accept-all/{accepted}")
    suspend fun acceptOrRejectAllInvitations(
        @Path("accepted") accepted: Boolean
    ): Response<GenericResponse>

    @Multipart
    @POST("v2/docs/uploadFiles")
    suspend fun uploadFiles(
        @Part("moduleName") moduleName: RequestBody,
        @Part("_id") id: RequestBody,
        @Part files: List<MultipartBody.Part>?
    ): Response<UploadFilesResponse>

    @Multipart
    @POST("v2/docs/upload")
    suspend fun uploadFilesV2(
        @Part files: List<MultipartBody.Part>?,
        @Part("moduleName") moduleName: RequestBody,
        @Part("moduleId") moduleId: RequestBody,
        @Part("metadata") metadata: RequestBody,
    ): Response<UploadFilesV2Response>

    @Multipart
    @POST("v2/task/upload/{event}/{taskId}")
    suspend fun uploadEventWithFilesV2(
        @Path("event") event: String,
        @Path("taskId") taskId: String,
        @Query("hasFiles") hasFiles: Boolean,
        @Part files: List<MultipartBody.Part>?,
        @Part("message") message: RequestBody,
        @Part("metadata") metadata: RequestBody,
    ): Response<EventV2Response>

    @Multipart
    @POST("v2/task/upload/{event}/{taskId}")
    suspend fun uploadEventWithoutFilesV2(
        @Path("event") event: String,
        @Path("taskId") taskId: String,
        @Query("hasFiles") hasFiles: Boolean,
        @Part("message") message: RequestBody
    ): Response<EventV2Response>

    @GET("v2/docs/viewFiles/{module}/{moduleId}")
    suspend fun getFilesByModuleId(
        @Path("module") module: String,
        @Path("moduleId") moduleId: String
    ): Response<GetAllFilesResponse>

    @GET("v1/users")
    suspend fun getAdminsOrUsersList(
        @Query("role") role: String
    ): Response<AdminUsersResponse>

    @POST("v2/users/contacts/sync")
    suspend fun syncContacts(
        @Body syncContactsRequest: SyncContactsRequest
    ): Response<GetContactsResponse>

    @POST("v2/users/{phoneNumber}/contacts/sync/{enabled}")
    suspend fun syncContactsEnabled(
        @Path("phoneNumber") phoneNumber: String,
        @Path("enabled") enabled: Boolean
    ): Response<ContactSyncEnableResponse>

    @GET("v2/users/contacts")
    suspend fun getAllConnectionsV2(
    ): Response<AllCeibroConnections>

    @GET("v2/users/recent/contacts")
    suspend fun getRecentCeibroConnections(): Response<RecentCeibroConnections>

    @POST("v2/users/contacts/block/{contactId}")
    suspend fun blockUser(
        @Path("contactId") contactId: String,
    ): Response<BlockUserResponse>

    @POST("v2/users/contacts/un-block/{contactId}")
    suspend fun unblockUser(
        @Path("contactId") contactId: String,
    ): Response<BlockUserResponse>

    @POST("v2/users/contacts/delete")
    suspend fun syncDeletedContacts(
        @Query("deleteAll") deleteAll: Boolean = false,
        @Body syncContactsRequest: SyncContactsRequest
    ): Response<GetContactsResponse>



    @POST("v2/users/groups")
    suspend fun createConnectionGroup(
        @Body connectionGroupRequest: NewConnectionGroupRequest
    ): Response<CeibroConnectionGroupV2>

    @GET("v2/users/groups")
    suspend fun getConnectionGroups(): Response<GetConnectionGroupsResponse>
}