package com.zstronics.ceibro.data.repos.dashboard

import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.repos.auth.signup.GenericResponse
import com.zstronics.ceibro.data.repos.dashboard.attachment.AttachmentUploadRequest
import com.zstronics.ceibro.data.repos.dashboard.attachment.GetAllFilesResponse
import com.zstronics.ceibro.data.repos.dashboard.attachment.UploadFilesResponse
import com.zstronics.ceibro.data.repos.dashboard.connections.AllConnectionsResponse
import com.zstronics.ceibro.data.repos.dashboard.connections.CountResponse
import com.zstronics.ceibro.data.repos.dashboard.invites.MyInvitations
import com.zstronics.ceibro.data.repos.dashboard.invites.SendInviteRequest

interface IDashboardRepository {
    suspend fun getAllConnections(): ApiResponse<AllConnectionsResponse>
    suspend fun getConnectionCount(): ApiResponse<CountResponse>
    suspend fun getAllInvites(): ApiResponse<MyInvitations>
    suspend fun sendInvite(sendInviteRequest: SendInviteRequest): ApiResponse<GenericResponse>
    suspend fun acceptOrRejectInvitation(accepted: Boolean, inviteId: String): ApiResponse<GenericResponse>
    suspend fun acceptOrRejectAllInvitations(accepted: Boolean): ApiResponse<GenericResponse>
    suspend fun uploadFiles(attachmentUploadRequest: AttachmentUploadRequest): ApiResponse<UploadFilesResponse>
    suspend fun getFilesByModuleId(
        module: String,
        moduleId: String
    ): ApiResponse<GetAllFilesResponse>
}