package com.zstronics.ceibro.data.repos.projects

import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.repos.projects.projectsmain.AllProjectsResponse
import com.zstronics.ceibro.data.repos.projects.projectsmain.ProjectsWithMembersResponse

interface IProjectRepository {
    suspend fun getProjects(publishStatus:String): ApiResponse<AllProjectsResponse>
    suspend fun getProjectsWithMembers(): ApiResponse<ProjectsWithMembersResponse>
}