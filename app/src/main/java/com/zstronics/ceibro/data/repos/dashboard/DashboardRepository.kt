package com.zstronics.ceibro.data.repos.dashboard

import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.base.BaseNetworkRepository
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
import com.zstronics.ceibro.data.repos.dashboard.contacts.BlockUserResponse
import com.zstronics.ceibro.data.repos.dashboard.contacts.ContactSyncEnableResponse
import com.zstronics.ceibro.data.repos.dashboard.contacts.GetContactsResponse
import com.zstronics.ceibro.data.repos.dashboard.contacts.SyncContactsRequest
import com.zstronics.ceibro.data.repos.dashboard.invites.MyInvitations
import com.zstronics.ceibro.data.repos.dashboard.invites.SendInviteRequest
import com.zstronics.ceibro.data.repos.task.models.v2.EventCommentOnlyUploadV2Request
import com.zstronics.ceibro.data.repos.task.models.v2.EventV2Response
import com.zstronics.ceibro.data.repos.task.models.v2.EventWithFileUploadV2Request
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
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

    override suspend fun getConnectionCount(): ApiResponse<CountResponse> = executeSafely(
        call =
        {
            service.getConnectionCount()
        }
    )

    override suspend fun getAllInvites(): ApiResponse<MyInvitations> = executeSafely(
        call =
        {
            service.getAllInvites()
        }
    )

    override suspend fun sendInvite(sendInviteRequest: SendInviteRequest): ApiResponse<GenericResponse> =
        executeSafely(
            call =
            {
                service.sendInvite(sendInviteRequest)
            }
        )

    override suspend fun acceptOrRejectInvitation(
        accepted: Boolean,
        inviteId: String
    ): ApiResponse<GenericResponse> =
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

    override suspend fun uploadFiles(attachmentUploadRequest: AttachmentUploadRequest): ApiResponse<UploadFilesResponse> {
        val moduleName = attachmentUploadRequest.moduleName
            .toRequestBody("text/plain".toMediaTypeOrNull())
        val id = attachmentUploadRequest._id.toRequestBody("text/plain".toMediaTypeOrNull())

        val parts = attachmentUploadRequest.files?.map { file ->
            val reqFile = file.asRequestBody(("image/" + file.extension).toMediaTypeOrNull())
            MultipartBody.Part.createFormData("files", file.name, reqFile)
        }
        return executeSafely(call = {
            service.uploadFiles(moduleName, id, parts)
        })
    }

    override suspend fun uploadFiles(attachmentUploadRequest: AttachmentUploadV2Request): ApiResponse<UploadFilesV2Response> {
        val moduleName = attachmentUploadRequest.moduleName
            .toRequestBody("text/plain".toMediaTypeOrNull())
        val moduleId =
            attachmentUploadRequest.moduleId.toRequestBody("text/plain".toMediaTypeOrNull())
        val metadata =
            attachmentUploadRequest.metadata.toRequestBody("text/plain".toMediaTypeOrNull())

        val parts = attachmentUploadRequest.files?.map { file ->
            val reqFile = file.asRequestBody(("image/" + file.extension).toMediaTypeOrNull())
            MultipartBody.Part.createFormData("files", file.name, reqFile)
        }
        return executeSafely(call = {
            service.uploadFilesV2(parts, moduleName, moduleId, metadata)
        })
    }

    override suspend fun uploadEventWithFilesV2(
        event: String,
        taskId: String,
        hasFiles: Boolean,
        eventWithFileUploadV2Request: EventWithFileUploadV2Request
    ): ApiResponse<EventV2Response> {
        val message = eventWithFileUploadV2Request.message
            .toRequestBody("text/plain".toMediaTypeOrNull())

        val metadata =
            eventWithFileUploadV2Request.metadata.toRequestBody("text/plain".toMediaTypeOrNull())

        val parts = eventWithFileUploadV2Request.files?.map { file ->
            val reqFile = file.asRequestBody(("image/" + file.extension).toMediaTypeOrNull())
            MultipartBody.Part.createFormData("files", file.name, reqFile)
        }
        return executeSafely(call = {
            service.uploadEventWithFilesV2(
                event = event,
                taskId = taskId,
                hasFiles = hasFiles,
                files = parts,
                message = message,
                metadata = metadata
            )
        })
    }

    override suspend fun uploadEventWithoutFilesV2(
        event: String,
        taskId: String,
        hasFiles: Boolean,
        eventCommentOnlyUploadV2Request: EventCommentOnlyUploadV2Request
    ): ApiResponse<EventV2Response> {
        val message = eventCommentOnlyUploadV2Request.message
            .toRequestBody("text/plain".toMediaTypeOrNull())

        return executeSafely(call = {
            service.uploadEventWithoutFilesV2(
                event = event,
                taskId = taskId,
                hasFiles = hasFiles,
                message = message
            )
        })
    }

    override suspend fun getFilesByModuleId(
        module: String,
        moduleId: String
    ): ApiResponse<GetAllFilesResponse> =
        executeSafely(call = {
            service.getFilesByModuleId(module, moduleId)
        })


    override suspend fun getAdminsOrUsersList(role: String): ApiResponse<AdminUsersResponse> =
        executeSafely(
            call =
            {
                service.getAdminsOrUsersList(role)
            }
        )

    override suspend fun syncContacts(
        contacts: SyncContactsRequest
    ): ApiResponse<GetContactsResponse> = executeSafely(
        call =
        {
            service.syncContacts(contacts)
        }
    )

    override suspend fun syncDeletedContacts(
        deleteAll: Boolean,
        contacts: SyncContactsRequest
    ): ApiResponse<GetContactsResponse> = executeSafely(
        call =
        {
            service.syncDeletedContacts(deleteAll, contacts)
        }
    )

    override suspend fun syncContactsEnabled(
        phoneNumber: String,
        enabled: Boolean
    ): ApiResponse<ContactSyncEnableResponse> = executeSafely(
        call =
        {
            service.syncContactsEnabled(phoneNumber, enabled)
        }
    )

    override suspend fun getAllConnectionsV2(): ApiResponse<AllCeibroConnections> =
        executeSafely(
            call =
            {
                service.getAllConnectionsV2()
            }
        )

    override suspend fun blockUser(contactId: String): ApiResponse<BlockUserResponse> =
        executeSafely(
            call =
            {
                service.blockUser(contactId)
            }
        )

    override suspend fun unblockUser(contactId: String): ApiResponse<BlockUserResponse> =
        executeSafely(
            call =
            {
                service.unblockUser(contactId)
            }
        )

    override suspend fun getRecentCeibroConnections(): ApiResponse<RecentCeibroConnections> =
        executeSafely(
            call =
            {
                service.getRecentCeibroConnections()
            }
        )


    override suspend fun createConnectionGroup(connectionGroupRequest: NewConnectionGroupRequest): ApiResponse<CeibroConnectionGroupV2> =
        executeSafely(
            call =
            {
                service.createConnectionGroup(connectionGroupRequest)
            }
        )


    override suspend fun getConnectionGroups(): ApiResponse<GetConnectionGroupsResponse> =
        executeSafely(
            call =
            {
                service.getConnectionGroups()
            }
        )


    override suspend fun deleteConnectionGroup(groupId: String): ApiResponse<GenericResponse> =
        executeSafely(
            call =
            {
                service.deleteConnectionGroup(groupId)
            }
        )


    override suspend fun updateConnectionGroup(groupId: String, connectionGroupRequest: NewConnectionGroupRequest): ApiResponse<CeibroConnectionGroupV2> =
        executeSafely(
            call =
            {
                service.updateConnectionGroup(groupId, connectionGroupRequest)
            }
        )


    override suspend fun updateConnectionGroupWithoutName(groupId: String, connectionGroupUpdateRequest: ConnectionGroupUpdateWithoutNameRequest): ApiResponse<CeibroConnectionGroupV2> =
        executeSafely(
            call =
            {
                service.updateConnectionGroupWithoutName(groupId, connectionGroupUpdateRequest)
            }
        )

   override suspend fun deleteConnectionGroupInBulk( deleteBulkGroupRequest: DeleteGroupInBulkRequest): ApiResponse<GenericResponse> =
        executeSafely(
            call =
            {
                service.deleteConnectionGroupsInBulk(deleteBulkGroupRequest)
            }
        )

}