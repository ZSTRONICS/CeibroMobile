package com.zstronics.ceibro.data.repos.projects.createNewProject


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse

@Keep
data class CreateNewProjectResponse(
    @SerializedName("createProject")
    val createProject: CreateProject
) : BaseResponse() {
    @Keep
    data class CreateProject(
        @SerializedName("chatCount")
        val chatCount: Int,
        @SerializedName("createdAt")
        val createdAt: String,
        @SerializedName("description")
        val description: String,
        @SerializedName("docsCount")
        val docsCount: Int,
        @SerializedName("dueDate")
        val dueDate: String,
        @SerializedName("extraStatus")
        val extraStatus: List<String>,
        @SerializedName("_id")
        val id: String,
        @SerializedName("inDraftState")
        val inDraftState: Boolean,
        @SerializedName("isDefault")
        val isDefault: Boolean,
        @SerializedName("location")
        val location: String,
        @SerializedName("owner")
        val owner: List<String>,
        @SerializedName("projectPhoto")
        val projectPhoto: String,
        @SerializedName("publishStatus")
        val publishStatus: String,
        @SerializedName("tasksCount")
        val tasksCount: Int,
        @SerializedName("title")
        val title: String,
        @SerializedName("updatedAt")
        val updatedAt: String,
        @SerializedName("usersCount")
        val usersCount: Int
    ) : BaseResponse()
}