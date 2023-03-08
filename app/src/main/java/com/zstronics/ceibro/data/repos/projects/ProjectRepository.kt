package com.zstronics.ceibro.data.repos.projects

import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.base.BaseNetworkRepository
import com.zstronics.ceibro.data.repos.projects.createNewProject.CreateNewProjectResponse
import com.zstronics.ceibro.data.repos.projects.createNewProject.CreateProjectRequest
import com.zstronics.ceibro.data.repos.projects.projectsmain.AllProjectsResponse
import com.zstronics.ceibro.data.repos.projects.projectsmain.ProjectMembersResponse
import com.zstronics.ceibro.data.repos.projects.projectsmain.ProjectsWithMembersResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
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
        val title = getRequestBody(createProjectRequest.title)
        val location = getRequestBody(createProjectRequest.location)
        val description = getRequestBody(createProjectRequest.description)
        val dueDate = getRequestBody(createProjectRequest.dueDate)
        val publishStatus = getRequestBody(createProjectRequest.publishStatus)

        val ownersList = createProjectRequest.owners.map {
            getRequestBody(it)
        }

        val extraStatus = createProjectRequest.extraStatus.map {
            getRequestBody(it)
        }

        val file = File(createProjectRequest.projectPhoto)
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
                    ownersList,
                    extraStatus
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
}