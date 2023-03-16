package com.zstronics.ceibro.data.repos.projects.documents


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import com.zstronics.ceibro.data.database.models.attachments.FilesAttachments
import com.zstronics.ceibro.data.repos.chat.room.Member
import com.zstronics.ceibro.data.repos.projects.group.ProjectGroup

@Keep
data class CreateProjectFolderResponse(
    @SerializedName("data")
    val folder: ProjectFolder
) : BaseResponse() {
    @Keep
    data class ProjectFolder(
        @SerializedName("access")
        var access: List<Member>,
        @SerializedName("createdAt")
        val createdAt: String,
        @SerializedName("creator")
        val creator: Member,
        @SerializedName("group")
        val group: List<ProjectGroup>?,
        @SerializedName("_id")
        val id: String,
        @SerializedName("name")
        val name: String,
        @SerializedName("parentFolder")
        val parentFolder: String,
        @SerializedName("project")
        val project: String,
        @SerializedName("updatedAt")
        val updatedAt: String,
        @SerializedName("files")
        var files: ArrayList<FilesAttachments>? = arrayListOf()
    ) : BaseResponse()
}