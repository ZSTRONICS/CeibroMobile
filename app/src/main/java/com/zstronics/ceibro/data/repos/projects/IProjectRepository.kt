package com.zstronics.ceibro.data.repos.projects

import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.base.BaseResponse
import com.zstronics.ceibro.data.repos.projects.createNewProject.CreateNewProjectResponse
import com.zstronics.ceibro.data.repos.projects.createNewProject.CreateProjectRequest
import com.zstronics.ceibro.data.repos.projects.group.CreateGroupRequest
import com.zstronics.ceibro.data.repos.projects.group.CreateProjectGroupResponse
import com.zstronics.ceibro.data.repos.projects.group.GetProjectGroupsResponse
import com.zstronics.ceibro.data.repos.projects.member.*
import com.zstronics.ceibro.data.repos.projects.projectsmain.AllProjectsResponse
import com.zstronics.ceibro.data.repos.projects.projectsmain.GetAvailableMemberResponse
import com.zstronics.ceibro.data.repos.projects.projectsmain.ProjectMembersResponse
import com.zstronics.ceibro.data.repos.projects.projectsmain.ProjectsWithMembersResponse
import com.zstronics.ceibro.data.repos.projects.role.CreateRoleRequest
import com.zstronics.ceibro.data.repos.projects.role.CreateRoleResponse
import com.zstronics.ceibro.data.repos.projects.role.ProjectRolesResponse

interface IProjectRepository {
    suspend fun getProjects(): ApiResponse<AllProjectsResponse>
    suspend fun getProjectsWithMembers(includeMe: Boolean): ApiResponse<ProjectsWithMembersResponse>
    suspend fun getMemberByProjectId(projectId: String): ApiResponse<ProjectMembersResponse>
    suspend fun createProject(createProjectRequest: CreateProjectRequest): ApiResponse<CreateNewProjectResponse>
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
}