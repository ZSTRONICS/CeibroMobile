package com.zstronics.ceibro.data.repos.projects


import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.base.BaseResponse
import com.zstronics.ceibro.data.repos.projects.createNewProject.CreateNewProjectResponse
import com.zstronics.ceibro.data.repos.projects.createNewProject.CreateProjectRequest
import com.zstronics.ceibro.data.repos.projects.createNewProject.UpdateProjectResponse
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
import okhttp3.MultipartBody
import okhttp3.RequestBody

interface IProjectRepository {
    suspend fun getProjects(): ApiResponse<AllProjectsResponse>
    suspend fun getProjectsWithMembers(includeMe: Boolean): ApiResponse<ProjectsWithMembersResponse>
    suspend fun createProject(createProjectRequest: CreateProjectRequest): ApiResponse<CreateNewProjectResponse>
    suspend fun updateProject(
        createProjectRequest: CreateProjectRequest,
        projectId: String
    ): ApiResponse<UpdateProjectResponse>

    suspend fun createGroup(
        projectId: String,
        body: CreateGroupRequest
    ): ApiResponse<CreateProjectGroupResponse>

    suspend fun updateGroup(
        groupId: String,
        body: CreateGroupRequest
    ): ApiResponse<CreateProjectGroupResponse>


    suspend fun getGroups(projectId: String): ApiResponse<GetProjectGroupsResponse>
    suspend fun getRoles(projectId: String): ApiResponse<ProjectRolesResponse>

    suspend fun createRoles(
        projectId: String,
        body: CreateRoleRequest
    ): ApiResponse<CreateRoleResponse>

    suspend fun updateRoles(
        projectId: String,
        body: CreateRoleRequest
    ): ApiResponse<CreateRoleResponse>

    suspend fun deleteRole(roleId: String): ApiResponse<BaseResponse>
    suspend fun deleteGroup(id: String): ApiResponse<BaseResponse>

    suspend fun createProjectMember(
        projectId: String,
        body: CreateProjectMemberRequest
    ): ApiResponse<CreateProjectMemberResponse>

    suspend fun getProjectMembers(
        projectId: String
    ): ApiResponse<GetProjectMemberResponse>

    suspend fun getAvailableMembers(
        projectId: String
    ): ApiResponse<GetAvailableMemberResponse>

    suspend fun deleteMember(id: String): ApiResponse<DeleteMemberResponse>
    suspend fun updateProjectMember(
        id: String,
        body: EditProjectMemberRequest
    ): ApiResponse<EditProjectMemberResponse>

    suspend fun createProjectFolder(
        projectId: String,
        folderName: String
    ): ApiResponse<CreateProjectFolderResponse>

    suspend fun getProjectDocuments(
        projectId: String
    ): ApiResponse<ProjectDocumentsResponse>

    suspend fun updateDocumentAccess(request: ManageProjectDocumentAccessRequest): ApiResponse<BaseResponse>

    suspend fun getProjectsV2(): ApiResponse<AllProjectsResponseV2>

    suspend fun createNewProjectWithFile(
        title: RequestBody,
        description: RequestBody,
        file: MultipartBody.Part
    ): ApiResponse<NewProjectResponseV2>

    suspend fun createNewProjectWithoutFile(
        title: RequestBody,
        description: RequestBody
    ): ApiResponse<NewProjectResponseV2>


    suspend fun updateHideProjectStatus(
        hidden: Boolean,
        projectId: String
    ): ApiResponse<UpdateProjectResponseV2>

    suspend fun updateFavoriteProjectStatus(
        favorite: Boolean,
        projectId: String
    ): ApiResponse<UpdateProjectResponseV2>


    //New APIS for groups module

    suspend fun getGroupsByProjectId(
        projectId: String
    ): ApiResponse<GetProjectGroupsResponseV2>

    suspend fun getFloorsByProjectTid(
        projectId: String
    ): ApiResponse<ProjectFloorResponseV2>

    suspend fun createGroupV2(
        projectId: String,
        groupName: CreateNewGroupV2Request,
    ): ApiResponse<CreateGroupResponseV2>

    suspend fun updateGroupByIdV2(
        projectId: String,
        groupName: CreateNewGroupV2Request,
    ): ApiResponse<CreateGroupResponseV2>

    suspend fun createFloorV2(
        projectId: String,
        floorName: CreateNewFloorRequest,
    ): ApiResponse<CreateFloorResponseV2>

    suspend fun deleteGroupByIdV2(
        groupId: String
    ): ApiResponse<DeleteGroupByIdResponseV2>
}