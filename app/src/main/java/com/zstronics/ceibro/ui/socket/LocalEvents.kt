package com.zstronics.ceibro.ui.socket

import com.zstronics.ceibro.data.repos.dashboard.attachment.AttachmentUploadRequest
import com.zstronics.ceibro.ui.attachment.SubtaskAttachment

object LocalEvents {
    class TaskCreatedEvent
    data class SubTaskCreatedEvent(val taskId: String)
    data class UploadFilesToServer(
        val request: AttachmentUploadRequest,
        val fileUriList: ArrayList<SubtaskAttachment?>
    )

    object AllFilesUploaded
    data class CreateNotification(
        val moduleId: String,
        val moduleName: String,
        val notificationTitle: String,
        val isOngoing: Boolean,
        val indeterminate: Boolean,
    )
}