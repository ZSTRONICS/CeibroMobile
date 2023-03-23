package com.zstronics.ceibro.data.repos.projects.documents


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import com.zstronics.ceibro.data.database.models.attachments.FilesAttachments
import com.zstronics.ceibro.data.repos.chat.room.Member

@Keep
data class ProjectDocumentsResponse(
    @SerializedName("result")
    val documentResult: ProjectDocument
) : BaseResponse() {
    @Keep
    data class ProjectDocument(
        @SerializedName("files")
        val files: List<FilesAttachments>,
        @SerializedName("folders")
        val folders: List<CreateProjectFolderResponse.ProjectFolder>
    ) : BaseResponse() {
        data class ProjectFiles(
            @SerializedName("_id")
            val id: String,
            @SerializedName("access")
            val access: List<Member>,
            @SerializedName("createdAt")
            val createdAt: String,
            @SerializedName("fileName")
            val fileName: String,
            @SerializedName("fileSize")
            val fileSize: Int,
            @SerializedName("fileType")
            val fileType: String,
            @SerializedName("fileUrl")
            val fileUrl: String,
            @SerializedName("moduleId")
            val moduleId: String,
            @SerializedName("moduleType")
            val moduleType: String,
            @SerializedName("updatedAt")
            val updatedAt: String,
            @SerializedName("uploadStatus")
            val uploadStatus: String,
            @SerializedName("uploadedBy")
            val uploadedBy: Member,
            @SerializedName("version")
            val version: Int
        )

    }
}