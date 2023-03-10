package com.zstronics.ceibro.data.repos.projects.createNewProject


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import com.zstronics.ceibro.data.repos.projects.projectsmain.AllProjectsResponse

@Keep
data class CreateNewProjectResponse(
    @SerializedName("createProject")
    val createProject: AllProjectsResponse.Projects
) : BaseResponse()