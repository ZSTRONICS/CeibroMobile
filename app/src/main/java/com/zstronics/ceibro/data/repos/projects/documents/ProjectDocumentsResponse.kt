package com.zstronics.ceibro.data.repos.projects.documents


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import com.zstronics.ceibro.data.database.models.attachments.FilesAttachments

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
    ) : BaseResponse()
}