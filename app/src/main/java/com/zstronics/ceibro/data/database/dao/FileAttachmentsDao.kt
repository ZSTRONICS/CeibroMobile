package com.zstronics.ceibro.data.database.dao

import androidx.room.*
import com.zstronics.ceibro.data.database.models.attachments.FilesAttachments

@Dao
interface FileAttachmentsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: FilesAttachments)

    @Query("SELECT * FROM files_attachments WHERE moduleId = :moduleId and moduleType = :moduleType")
    suspend fun getAttachmentsById(moduleType: String, moduleId: String): List<FilesAttachments>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<FilesAttachments>)

    @Query("DELETE FROM files_attachments")
    suspend fun deleteAllFileAttachments()

    @Update
    suspend fun updateAttachment(filesAttachments: FilesAttachments)

    @Query("DELETE FROM files_attachments WHERE id = :id")
    suspend fun deleteAttachmentById(id: String)

    @Update
    suspend fun updateFileAttachments(fileAttachments: List<FilesAttachments>)
}
