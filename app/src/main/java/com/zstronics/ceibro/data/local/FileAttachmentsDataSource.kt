package com.zstronics.ceibro.data.local

import com.zstronics.ceibro.data.database.dao.FileAttachmentsDao
import com.zstronics.ceibro.data.database.dao.SubTaskDao
import com.zstronics.ceibro.data.database.models.attachments.FilesAttachments
import javax.inject.Inject

class FileAttachmentsDataSource @Inject constructor(private val dao: FileAttachmentsDao) {
    suspend fun insertFile(file: FilesAttachments) {
        dao.insertFile(file)
    }

    suspend fun getAttachmentsById(moduleType: String, moduleId: String): List<FilesAttachments> =
        dao.getAttachmentsById(moduleType, moduleId)

    suspend fun insertAll(list: List<FilesAttachments>) = dao.insertAll(list)
    suspend fun deleteAllFileAttachments() = dao.deleteAllFileAttachments()
    suspend fun updateAttachment(filesAttachments: FilesAttachments) =
        dao.updateAttachment(filesAttachments)

    suspend fun deleteAttachmentById(id: String) = dao.deleteAttachmentById(id)
}