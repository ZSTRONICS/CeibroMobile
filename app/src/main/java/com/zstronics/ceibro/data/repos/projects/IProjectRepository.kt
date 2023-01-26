package com.zstronics.ceibro.data.repos.projects

import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.repos.projects.projectsmain.AllProjectsResponse
import com.zstronics.ceibro.data.repos.projects.projectsmain.ProjectMembersResponse
import com.zstronics.ceibro.data.repos.projects.projectsmain.ProjectsWithMembersResponse

interface IProjectRepository {
    suspend fun getProjects(publishStatus:String): ApiResponse<AllProjectsResponse>
    suspend fun getProjectsWithMembers(includeMe: Boolean): ApiResponse<ProjectsWithMembersResponse>
    suspend fun getMemberByProjectId(projectId: String): ApiResponse<ProjectMembersResponse>
}