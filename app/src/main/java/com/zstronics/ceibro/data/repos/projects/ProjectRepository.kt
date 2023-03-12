package com.zstronics.ceibro.data.repos.projects

import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.base.BaseNetworkRepository
import com.zstronics.ceibro.data.base.BaseResponse
import com.zstronics.ceibro.data.repos.projects.createNewProject.CreateNewProjectResponse
import com.zstronics.ceibro.data.repos.projects.createNewProject.CreateProjectRequest
import com.zstronics.ceibro.data.repos.projects.group.CreateGroupRequest
import com.zstronics.ceibro.data.repos.projects.group.CreateProjectGroupResponse
import com.zstronics.ceibro.data.repos.projects.group.GetProjectGroupsResponse
import com.zstronics.ceibro.data.repos.projects.projectsmain.AllProjectsResponse
import com.zstronics.ceibro.data.repos.projects.projectsmain.ProjectMembersResponse
import com.zstronics.ceibro.data.repos.projects.projectsmain.ProjectsWithMembersResponse
import com.zstronics.ceibro.data.repos.projects.role.CreateRoleRequest
import com.zstronics.ceibro.data.repos.projects.role.CreateRoleResponse
import com.zstronics.ceibro.data.repos.projects.role.ProjectRolesResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import javax.inject.Inject

class ProjectRepository @Inject constructor(
    private val service: ProjectRepositoryService
) : IProjectRepository, BaseNetworkRepository() {
    override suspend fun getProjects(): ApiResponse<AllProjectsResponse> =
        executeSafely(
            call =
            {
                service.getProjects()
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
        val title = getRequestBody(createProjectRequest.title)
        val location = getRequestBody(createProjectRequest.location)
        val description = getRequestBody(createProjectRequest.description)
        val dueDate = getRequestBody(createProjectRequest.dueDate)
        val publishStatus = getRequestBody(createProjectRequest.publishStatus)

        val file = createProjectRequest.projectPhoto
        val reqFile = file.asRequestBody(("image/" + file.extension).toMediaTypeOrNull())
        val projectPhoto: MultipartBody.Part =
            MultipartBody.Part.createFormData("projectPhoto", file.name, reqFile)

        return executeSafely(
            call = {
                service.createProject(
                    projectPhoto,
                    title,
                    location,
                    description,
                    dueDate,
                    publishStatus,
                    getRequestBody(createProjectRequest.extraStatus),
                    getRequestBody(createProjectRequest.owner)
                )
            }
        )
    }

    private fun getRequestBody(value: String): RequestBody {
        return RequestBody.create(
            "text/plain".toMediaTypeOrNull(),
            value
        )
    }

    override suspend fun createGroup(
        projectId: String,
        body: CreateGroupRequest
    ): ApiResponse<CreateProjectGroupResponse> =
        executeSafely {
            service.createGroup(projectId, body)

        }

    override suspend fun getGroups(projectId: String): ApiResponse<GetProjectGroupsResponse> =
        executeSafely {
            service.getGroups(projectId)
        }

    override suspend fun getRoles(projectId: String): ApiResponse<ProjectRolesResponse> =
        executeSafely {
            service.getRoles(projectId)
        }

    override suspend fun createRoles(
        projectId: String,
        body: CreateRoleRequest
    ): ApiResponse<CreateRoleResponse> = executeSafely {
        service.createRoles(projectId, body)
    }
    override suspend fun updateRoles(
        projectId: String,
        body: CreateRoleRequest
    ): ApiResponse<CreateRoleResponse> = executeSafely {
        service.updateRoles(projectId, body)
    }

    override suspend fun deleteRole(roleId: String): ApiResponse<BaseResponse> = executeSafely {
        service.deleteRole(roleId)
    }
}