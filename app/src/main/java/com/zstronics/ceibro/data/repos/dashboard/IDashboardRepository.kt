package com.zstronics.ceibro.data.repos.dashboard

import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.repos.auth.refreshtoken.TokenValidityResponse
import com.zstronics.ceibro.data.repos.auth.signup.GenericResponse
import com.zstronics.ceibro.data.repos.dashboard.admins.AdminUsersResponse
import com.zstronics.ceibro.data.repos.dashboard.attachment.AttachmentUploadRequest
import com.zstronics.ceibro.data.repos.dashboard.attachment.GetAllFilesResponse
import com.zstronics.ceibro.data.repos.dashboard.attachment.UploadFilesResponse
import com.zstronics.ceibro.data.repos.dashboard.attachment.v2.AttachmentUploadV2Request
import com.zstronics.ceibro.data.repos.dashboard.attachment.v2.UploadFilesV2Response
import com.zstronics.ceibro.data.repos.dashboard.connections.AllConnectionsResponse
import com.zstronics.ceibro.data.repos.dashboard.connections.CountResponse
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.AllCeibroConnections
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.CeibroConnectionGroupV2
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.ConnectionGroupUpdateWithoutNameRequest
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.DeleteGroupInBulkRequest
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.GetConnectionGroupsResponse
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.NewConnectionGroupRequest
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.RecentCeibroConnections
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.CreateGroupRequest
import com.zstronics.ceibro.data.repos.dashboard.contacts.BlockUserResponse
import com.zstronics.ceibro.data.repos.dashboard.contacts.ContactSyncEnableResponse
import com.zstronics.ceibro.data.repos.dashboard.contacts.GetContactsResponse
import com.zstronics.ceibro.data.repos.dashboard.contacts.SyncContactsRequest
import com.zstronics.ceibro.data.repos.dashboard.invites.MyInvitations
import com.zstronics.ceibro.data.repos.dashboard.invites.SendInviteRequest
import com.zstronics.ceibro.data.repos.task.models.v2.EventCommentOnlyUploadV2Request
import com.zstronics.ceibro.data.repos.task.models.v2.EventV2Response
import com.zstronics.ceibro.data.repos.task.models.v2.EventWithFileUploadV2Request

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
    suspend fun uploadFiles(attachmentUploadRequest: AttachmentUploadV2Request): ApiResponse<UploadFilesV2Response>

    suspend fun uploadEventWithFilesV2(
        event: String,
        taskId: String,
        hasFiles: Boolean,
        eventWithFileUploadV2Request: EventWithFileUploadV2Request
    ): ApiResponse<EventV2Response>

    suspend fun uploadEventWithoutFilesV2(
        event: String,
        taskId: String,
        hasFiles: Boolean,
        eventCommentOnlyUploadV2Request: EventCommentOnlyUploadV2Request
    ): ApiResponse<EventV2Response>

    suspend fun getFilesByModuleId(
        module: String,
        moduleId: String
    ): ApiResponse<GetAllFilesResponse>

    suspend fun getAdminsOrUsersList(role: String): ApiResponse<AdminUsersResponse>
    suspend fun syncContacts(
        contacts: SyncContactsRequest
    ): ApiResponse<GetContactsResponse>

    suspend fun syncDeletedContacts(
        deleteAll: Boolean,
        contacts: SyncContactsRequest
    ): ApiResponse<GetContactsResponse>

    suspend fun syncContactsEnabled(
        phoneNumber: String,
        enabled: Boolean
    ): ApiResponse<ContactSyncEnableResponse>

    suspend fun getAllConnectionsV2(
    ): ApiResponse<AllCeibroConnections>

    suspend fun getRecentCeibroConnections(): ApiResponse<RecentCeibroConnections>

    suspend fun blockUser(
        contactId: String,
    ): ApiResponse<BlockUserResponse>

    suspend fun unblockUser(
        contactId: String,
    ): ApiResponse<BlockUserResponse>

    suspend fun createConnectionGroup(
        connectionGroupRequest: CreateGroupRequest
    ): ApiResponse<CeibroConnectionGroupV2>

    suspend fun getConnectionGroups(): ApiResponse<GetConnectionGroupsResponse>

    suspend fun deleteConnectionGroup(groupId: String): ApiResponse<GenericResponse>

    suspend fun updateConnectionGroup(
        groupId: String,
        connectionGroupRequest: CreateGroupRequest
    ): ApiResponse<CeibroConnectionGroupV2>

    suspend fun updateConnectionGroupWithoutName(
        groupId: String,
        connectionGroupUpdateRequest: ConnectionGroupUpdateWithoutNameRequest
    ): ApiResponse<CeibroConnectionGroupV2>

    suspend fun deleteConnectionGroupInBulk(
        deleteBulkGroupRequest: DeleteGroupInBulkRequest
    ): ApiResponse<GenericResponse>

    suspend fun validateUserToken(): ApiResponse<TokenValidityResponse>
}