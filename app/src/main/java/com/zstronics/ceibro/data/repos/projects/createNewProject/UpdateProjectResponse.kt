package com.zstronics.ceibro.data.repos.projects.createNewProject


import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import com.zstronics.ceibro.data.repos.projects.projectsmain.AllProjectsResponse

data class UpdateProjectResponse(
    @SerializedName("result")
    val updatedProject: AllProjectsResponse.Projects
) : BaseResponse() {
    data class UpdateProject(
        @SerializedName("access")
        val access: List<String>,
        @SerializedName("chatCount")
        val chatCount: Int,
        @SerializedName("createdAt")
        val createdAt: String,
        @SerializedName("creator")
        val creator: Creator,
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
        val owner: List<Owner>,
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
    ) : BaseResponse() {
        data class Creator(
            @SerializedName("firstName")
            val firstName: String,
            @SerializedName("_id")
            val id: String,
            @SerializedName("surName")
            val surName: String
        ) : BaseResponse()

        data class Owner(
            @SerializedName("firstName")
            val firstName: String,
            @SerializedName("_id")
            val id: String,
            @SerializedName("surName")
            val surName: String
        ) : BaseResponse()
    }
}