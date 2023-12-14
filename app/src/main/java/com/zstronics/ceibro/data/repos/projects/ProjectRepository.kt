package com.zstronics.ceibro.data.repos.projects


import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.base.BaseNetworkRepository
import com.zstronics.ceibro.data.base.BaseResponse
import com.zstronics.ceibro.data.repos.projects.createNewProject.CreateNewProjectResponse
import com.zstronics.ceibro.data.repos.projects.createNewProject.CreateProjectRequest
import com.zstronics.ceibro.data.repos.projects.createNewProject.UpdateProjectResponse
import com.zstronics.ceibro.data.repos.projects.documents.CreateProjectFolderRequest
import com.zstronics.ceibro.data.repos.projects.documents.CreateProjectFolderResponse
import com.zstronics.ceibro.data.repos.projects.documents.ManageProjectDocumentAccessRequest
import com.zstronics.ceibro.data.repos.projects.documents.ProjectDocumentsResponse
import com.zstronics.ceibro.data.repos.projects.floor.CreateFloorResponseV2
import com.zstronics.ceibro.data.repos.projects.floor.CreateNewFloorRequest
import com.zstronics.ceibro.data.repos.projects.group.CreateGroupRequest
import com.zstronics.ceibro.data.repos.projects.group.CreateGroupResponseV2
import com.zstronics.ceibro.data.repos.projects.group.CreateNewGroupV2Request
import com.zstronics.ceibro.data.repos.projects.group.CreateProjectGroupResponse
import com.zstronics.ceibro.data.repos.projects.group.DeleteGroupByIdResponseV2
import com.zstronics.ceibro.data.repos.projects.group.GetProjectGroupsResponse
import com.zstronics.ceibro.data.repos.projects.group.GetProjectGroupsResponseV2
import com.zstronics.ceibro.data.repos.projects.member.CreateProjectMemberRequest
import com.zstronics.ceibro.data.repos.projects.member.CreateProjectMemberResponse
import com.zstronics.ceibro.data.repos.projects.member.DeleteMemberResponse
import com.zstronics.ceibro.data.repos.projects.member.EditProjectMemberRequest
import com.zstronics.ceibro.data.repos.projects.member.EditProjectMemberResponse
import com.zstronics.ceibro.data.repos.projects.member.GetProjectMemberResponse
import com.zstronics.ceibro.data.repos.projects.projectsmain.AllProjectsResponse
import com.zstronics.ceibro.data.repos.projects.projectsmain.GetAvailableMemberResponse
import com.zstronics.ceibro.data.repos.projects.projectsmain.ProjectFloorResponseV2
import com.zstronics.ceibro.data.repos.projects.projectsmain.ProjectsWithMembersResponse
import com.zstronics.ceibro.data.repos.projects.role.CreateRoleRequest
import com.zstronics.ceibro.data.repos.projects.role.CreateRoleResponse
import com.zstronics.ceibro.data.repos.projects.role.ProjectRolesResponse
import com.zstronics.ceibro.data.repos.projects.v2.AllProjectsResponseV2
import com.zstronics.ceibro.data.repos.projects.v2.NewProjectResponseV2
import com.zstronics.ceibro.data.repos.projects.v2.UpdateProjectResponseV2
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.PATCH
import retrofit2.http.Path
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


    override suspend fun createProject(createProjectRequest: CreateProjectRequest): ApiResponse<CreateNewProjectResponse> {
        val title = getRequestBody(createProjectRequest.title)
        val location = getRequestBody(createProjectRequest.location)
        val description = getRequestBody(createProjectRequest.description)
        val dueDate = getRequestBody(createProjectRequest.dueDate)
        val publishStatus = getRequestBody(createProjectRequest.publishStatus)

        val file = createProjectRequest.projectPhoto
        val reqFile = file?.asRequestBody(("image/" + file.extension).toMediaTypeOrNull())
        val projectPhoto: MultipartBody.Part? =
            reqFile?.let { MultipartBody.Part.createFormData("projectPhoto", file.name, it) }

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

    override suspend fun updateProject(
        createProjectRequest: CreateProjectRequest,
        projectId: String
    ): ApiResponse<UpdateProjectResponse> {
        val title = getRequestBodyOrNull(createProjectRequest.title)
        val location = getRequestBodyOrNull(createProjectRequest.location)
        val description = getRequestBodyOrNull(createProjectRequest.description)
        val dueDate = getRequestBodyOrNull(createProjectRequest.dueDate)
        val publishStatus = getRequestBodyOrNull(createProjectRequest.publishStatus)

        val file = createProjectRequest.projectPhoto
        val reqFile = file?.asRequestBody(("image/" + file.extension).toMediaTypeOrNull())
        val projectPhoto: MultipartBody.Part? =
            reqFile?.let { MultipartBody.Part.createFormData("projectPhoto", file.name, it) }

        return executeSafely(
            call = {
                if (file != null)
                    service.updateProject(
                        projectId,
                        projectPhoto,
                        title,
                        location,
                        description,
                        dueDate,
                        publishStatus,
                        getRequestBodyOrNull(createProjectRequest.extraStatus),
                        getRequestBodyOrNull(createProjectRequest.owner)
                    )
                else
                    service.updateProject(
                        projectId,
                        title,
                        location,
                        description,
                        dueDate,
                        publishStatus,
                        getRequestBodyOrNull(createProjectRequest.extraStatus),
                        getRequestBodyOrNull(createProjectRequest.owner)
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

    private fun getRequestBodyOrNull(value: String): RequestBody? {
        if (value.isNullOrEmpty())
            return null
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

    override suspend fun updateGroup(
        groupId: String,
        body: CreateGroupRequest
    ): ApiResponse<CreateProjectGroupResponse> =
        executeSafely {
            service.updateGroup(groupId, body)
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

    override suspend fun deleteGroup(id: String): ApiResponse<BaseResponse> = executeSafely {
        service.deleteGroup(id)
    }

    override suspend fun createProjectMember(
        projectId: String,
        body: CreateProjectMemberRequest
    ): ApiResponse<CreateProjectMemberResponse> = executeSafely {
        service.createProjectMember(projectId, body)
    }

    override suspend fun getProjectMembers(projectId: String): ApiResponse<GetProjectMemberResponse> =
        executeSafely {
            service.getProjectMembers(projectId)
        }

    override suspend fun getAvailableMembers(projectId: String): ApiResponse<GetAvailableMemberResponse> =
        executeSafely {
            service.getAvailableMembers(projectId)
        }

    override suspend fun deleteMember(id: String): ApiResponse<DeleteMemberResponse> =
        executeSafely {
            service.deleteMember(id)
        }

    override suspend fun updateProjectMember(
        id: String,
        body: EditProjectMemberRequest
    ): ApiResponse<EditProjectMemberResponse> = executeSafely {
        service.updateProjectMember(id, body)
    }

    override suspend fun createProjectFolder(
        projectId: String,
        folderName: String
    ): ApiResponse<CreateProjectFolderResponse> = executeSafely {
        service.createProjectFolder(projectId, CreateProjectFolderRequest(folderName))
    }

    override suspend fun getProjectDocuments(
        projectId: String
    ): ApiResponse<ProjectDocumentsResponse> = executeSafely {
        service.getProjectDocuments(projectId)
    }

    override suspend fun updateDocumentAccess(request: ManageProjectDocumentAccessRequest): ApiResponse<BaseResponse> =
        executeSafely {
            service.updateDocumentAccess(request.fileOrFolderId, request)
        }


    override suspend fun getProjectsV2(): ApiResponse<AllProjectsResponseV2> =
        executeSafely(
            call =
            {
                service.getProjectsV2()
            }
        )

    override suspend fun createNewProjectWithFile(
        title: RequestBody,
        description: RequestBody,
        file: MultipartBody.Part
    ): ApiResponse<NewProjectResponseV2> {
        return executeSafely(call = {
            service.createNewProjectWithFile(
                title = title,
                description = description,
                file = file
            )
        })
    }

    override suspend fun createNewProjectWithoutFile(
        title: RequestBody,
        description: RequestBody
    ): ApiResponse<NewProjectResponseV2> {
        return executeSafely(call = {
            service.createNewProjectWithoutFile(
                title = title,
                description = description
            )
        })
    }

    override suspend fun updateHideProjectStatus(
        hidden: Boolean,
        projectId: String
    ): ApiResponse<UpdateProjectResponseV2> {
        return executeSafely(call = {
            service.updateHideProjectStatus(
                state = hidden,
                projectId = projectId
            )
        })
    }

    override suspend fun updateFavoriteProjectStatus(
        favorite: Boolean,
        projectId: String
    ): ApiResponse<UpdateProjectResponseV2> {
        return executeSafely(call = {
            service.updateFavoriteProjectStatus(
                state = favorite,
                projectId = projectId
            )
        })
    }


    //New APIS for project
    override suspend fun getGroupsByProjectTid(
        projectId: String
    ): ApiResponse<GetProjectGroupsResponseV2> {
        return executeSafely(call = {
            service.getGroupsByProjectTid(
                projectId = projectId
            )
        })
    }
    override suspend fun getFloorsByProjectTid(
        projectId: String
    ): ApiResponse<ProjectFloorResponseV2> {
        return executeSafely(call = {
            service.getFloorsByProjectTid(
                projectId = projectId
            )
        })
    }
    override suspend fun createGroupV2(
        projectId: String,
        groupName: CreateNewGroupV2Request,
    ): ApiResponse<CreateGroupResponseV2> {
        return executeSafely(call = {
            service.createGroupV2(
                projectId = projectId,groupName
            )
        })
    }


    override suspend fun updateGroupByIdV2(
        @Path("groupId") groupId: String,
        @Body body: CreateNewGroupV2Request
    ): ApiResponse<CreateGroupResponseV2> {
        return executeSafely(call = {
            service.updateGroupByIdV2(
               groupId,body
            )
        })
    }

    override suspend fun createFloorV2(
        projectId: String,
        floorName: CreateNewFloorRequest,
    ): ApiResponse<CreateFloorResponseV2> {
        return executeSafely(call = {
            service.createFloorV2(
                projectId = projectId,floorName
            )
        })
    }

    override suspend fun deleteGroupByIdV2(
        groupId: String,
    ): ApiResponse<DeleteGroupByIdResponseV2> {
        return executeSafely(call = {
            service.deleteGroupByIdV2(
                groupId = groupId
            )
        })
    }
}