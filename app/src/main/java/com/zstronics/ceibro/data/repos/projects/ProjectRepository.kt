package com.zstronics.ceibro.data.repos.projects

import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.base.BaseNetworkRepository
import com.zstronics.ceibro.data.repos.projects.createNewProject.CreateNewProjectResponse
import com.zstronics.ceibro.data.repos.projects.createNewProject.CreateProjectRequest
import com.zstronics.ceibro.data.repos.projects.projectsmain.AllProjectsResponse
import com.zstronics.ceibro.data.repos.projects.projectsmain.ProjectMembersResponse
import com.zstronics.ceibro.data.repos.projects.projectsmain.ProjectsWithMembersResponse
import javax.inject.Inject

class ProjectRepository @Inject constructor(
    private val service: ProjectRepositoryService
) : IProjectRepository, BaseNetworkRepository() {
    override suspend fun getProjects(publishStatus: String): ApiResponse<AllProjectsResponse> =
        executeSafely(
            call =
            {
                service.getProjects(publishStatus)
            }
        )

    override suspend fun getProjectsWithMembers(includeMe: Boolean): ApiResponse<ProjectsWithMembersResponse> =
        executeSafely(
            call =
            {
                service.getProjectsWithMembers(includeMe)
            }
        )

    override suspend fun getMemberByProjectId(projectId: String): ApiResponse<ProjectMembersResponse> =
        executeSafely(
            call = { service.getMemberByProjectId(projectId) }
        )

    override suspend fun createProject(createProjectRequest: CreateProjectRequest): ApiResponse<CreateNewProjectResponse> {
        return executeSafely(
            call = {
                service.createProject(
                    createProjectRequest
                )
            }
        )
    }
}