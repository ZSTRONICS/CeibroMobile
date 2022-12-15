package com.zstronics.ceibro.data.repos.projects

import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.base.BaseNetworkRepository
import com.zstronics.ceibro.data.repos.projects.projectsmain.AllProjectsResponse
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

    override suspend fun getProjectsWithMembers(): ApiResponse<ProjectsWithMembersResponse> = executeSafely(
        call =
        {
            service.getProjectsWithMembers()
        }
    )

}