package com.zstronics.ceibro.data.repos.dashboard

import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.base.BaseNetworkRepository
import com.zstronics.ceibro.data.repos.auth.signup.GenericResponse
import com.zstronics.ceibro.data.repos.dashboard.admins.AdminUsersResponse
import com.zstronics.ceibro.data.repos.dashboard.attachment.AttachmentUploadRequest
import com.zstronics.ceibro.data.repos.dashboard.attachment.GetAllFilesResponse
import com.zstronics.ceibro.data.repos.dashboard.attachment.UploadFilesResponse
import com.zstronics.ceibro.data.repos.dashboard.connections.AllConnectionsResponse
import com.zstronics.ceibro.data.repos.dashboard.connections.CountResponse
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.AllCeibroConnections
import com.zstronics.ceibro.data.repos.dashboard.contacts.BlockUserResponse
import com.zstronics.ceibro.data.repos.dashboard.contacts.ContactSyncEnableResponse
import com.zstronics.ceibro.data.repos.dashboard.contacts.GetContactsResponse
import com.zstronics.ceibro.data.repos.dashboard.contacts.SyncContactsRequest
import com.zstronics.ceibro.data.repos.dashboard.invites.MyInvitations
import com.zstronics.ceibro.data.repos.dashboard.invites.SendInviteRequest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
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
        val moduleName = RequestBody.create(
            "text/plain".toMediaTypeOrNull(),
            attachmentUploadRequest.moduleName
        )
        val id = RequestBody.create("text/plain".toMediaTypeOrNull(), attachmentUploadRequest._id)

        val parts = attachmentUploadRequest.files?.map { file ->
            val reqFile = file.asRequestBody(("image/" + file.extension).toMediaTypeOrNull())
            MultipartBody.Part.createFormData("files", file.name, reqFile)
        }
        return executeSafely(call = {
            service.uploadFiles(moduleName, id, parts)
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
        userId: String,
        contacts: SyncContactsRequest
    ): ApiResponse<GetContactsResponse> = executeSafely(
        call =
        {
            service.syncContacts(userId, contacts)
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

    override suspend fun getAllConnectionsV2(userId: String): ApiResponse<AllCeibroConnections> =
        executeSafely(
            call =
            {
                service.getAllConnectionsV2(userId)
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
}