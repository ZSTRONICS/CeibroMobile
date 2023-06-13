package com.zstronics.ceibro.data.repos.dashboard

import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.repos.auth.signup.GenericResponse
import com.zstronics.ceibro.data.repos.dashboard.admins.AdminUsersResponse
import com.zstronics.ceibro.data.repos.dashboard.attachment.AttachmentUploadRequest
import com.zstronics.ceibro.data.repos.dashboard.attachment.GetAllFilesResponse
import com.zstronics.ceibro.data.repos.dashboard.attachment.UploadFilesResponse
import com.zstronics.ceibro.data.repos.dashboard.attachment.v2.AttachmentUploadV2Request
import com.zstronics.ceibro.data.repos.dashboard.connections.AllConnectionsResponse
import com.zstronics.ceibro.data.repos.dashboard.connections.CountResponse
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.AllCeibroConnections
import com.zstronics.ceibro.data.repos.dashboard.contacts.BlockUserResponse
import com.zstronics.ceibro.data.repos.dashboard.contacts.ContactSyncEnableResponse
import com.zstronics.ceibro.data.repos.dashboard.contacts.GetContactsResponse
import com.zstronics.ceibro.data.repos.dashboard.contacts.SyncContactsRequest
import com.zstronics.ceibro.data.repos.dashboard.invites.MyInvitations
import com.zstronics.ceibro.data.repos.dashboard.invites.SendInviteRequest

interface IDashboardRepository {
    suspend fun getAllConnections(): ApiResponse<AllConnectionsResponse>
    suspend fun getConnectionCount(): ApiResponse<CountResponse>
    suspend fun getAllInvites(): ApiResponse<MyInvitations>
    suspend fun sendInvite(sendInviteRequest: SendInviteRequest): ApiResponse<GenericResponse>
    suspend fun acceptOrRejectInvitation(
        accepted: Boolean,
        inviteId: String
    ): ApiResponse<GenericResponse>

    suspend fun acceptOrRejectAllInvitations(accepted: Boolean): ApiResponse<GenericResponse>
    suspend fun uploadFiles(attachmentUploadRequest: AttachmentUploadRequest): ApiResponse<UploadFilesResponse>
    suspend fun uploadFiles(attachmentUploadRequest: AttachmentUploadV2Request): ApiResponse<UploadFilesResponse>
    suspend fun getFilesByModuleId(
        module: String,
        moduleId: String
    ): ApiResponse<GetAllFilesResponse>

    suspend fun getAdminsOrUsersList(role: String): ApiResponse<AdminUsersResponse>
    suspend fun syncContacts(
        userId: String,
        contacts: SyncContactsRequest
    ): ApiResponse<GetContactsResponse>

    suspend fun syncContactsEnabled(
        phoneNumber: String,
        enabled: Boolean
    ): ApiResponse<ContactSyncEnableResponse>

    suspend fun getAllConnectionsV2(
        userId: String,
    ): ApiResponse<AllCeibroConnections>

    suspend fun blockUser(
        contactId: String,
    ): ApiResponse<BlockUserResponse>

    suspend fun unblockUser(
        contactId: String,
    ): ApiResponse<BlockUserResponse>
}