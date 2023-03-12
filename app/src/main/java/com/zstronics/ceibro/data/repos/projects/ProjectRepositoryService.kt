package com.zstronics.ceibro.data.repos.projects

import com.zstronics.ceibro.data.base.BaseResponse
import com.zstronics.ceibro.data.repos.projects.createNewProject.CreateNewProjectResponse
import com.zstronics.ceibro.data.repos.projects.group.CreateGroupRequest
import com.zstronics.ceibro.data.repos.projects.group.CreateProjectGroupResponse
import com.zstronics.ceibro.data.repos.projects.group.GetProjectGroupsResponse
import com.zstronics.ceibro.data.repos.projects.member.CreateProjectMemberRequest
import com.zstronics.ceibro.data.repos.projects.member.CreateProjectMemberResponse
import com.zstronics.ceibro.data.repos.projects.member.GetProjectMemberResponse
import com.zstronics.ceibro.data.repos.projects.projectsmain.AllProjectsResponse
import com.zstronics.ceibro.data.repos.projects.projectsmain.ProjectMembersResponse
import com.zstronics.ceibro.data.repos.projects.projectsmain.ProjectsWithMembersResponse
import com.zstronics.ceibro.data.repos.projects.role.CreateRoleRequest
import com.zstronics.ceibro.data.repos.projects.role.CreateRoleResponse
import com.zstronics.ceibro.data.repos.projects.role.ProjectRolesResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ProjectRepositoryService {
    @GET("project")
    suspend fun getProjects(): Response<AllProjectsResponse>

    @POST("project/getProjectsWithMembers")
    suspend fun getProjectsWithMembers(@Query("includeMe") includeMe: Boolean = false): Response<ProjectsWithMembersResponse>

    @GET("project/member/{id}")
    suspend fun getMemberByProjectId(@Path("id") projectId: String): Response<ProjectMembersResponse>

    @Multipart
    @POST("project")
    suspend fun createProject(
        @Part projectPhoto: MultipartBody.Part,
        @Part("title") title: RequestBody,
        @Part("location") location: RequestBody,
        @Part("description") description: RequestBody,
        @Part("dueDate") dueDate: RequestBody,
        @Part("publishStatus") publishStatus: RequestBody,
        @Part("extraStatus") extraStatus: RequestBody,
        @Part("owner") owner: RequestBody
    ): Response<CreateNewProjectResponse>

    @POST("project/group/{id}")
    suspend fun createGroup(
        @Path("id") projectId: String,
        @Body body: CreateGroupRequest
    ): Response<CreateProjectGroupResponse>

    @PATCH("project/group/{id}")
    suspend fun updateGroup(
        @Path("id") groupId: String,
        @Body body: CreateGroupRequest
    ): Response<CreateProjectGroupResponse>

    @DELETE("project/group/{id}")
    suspend fun deleteGroup(@Path("id") id: String): Response<BaseResponse>

    @GET("project/group/{id}")
    suspend fun getGroups(@Path("id") projectId: String): Response<GetProjectGroupsResponse>

    @GET("project/role/{id}")
    suspend fun getRoles(@Path("id") projectId: String): Response<ProjectRolesResponse>

    @POST("project/role/{id}")
    suspend fun createRoles(
        @Path("id") projectId: String,
        @Body body: CreateRoleRequest
    ): Response<CreateRoleResponse>

    @PATCH("project/role/{id}")
    suspend fun updateRoles(
        @Path("id") projectId: String,
        @Body body: CreateRoleRequest
    ): Response<CreateRoleResponse>

    @DELETE("project/role/{id}")
    suspend fun deleteRole(@Path("id") roleId: String): Response<BaseResponse>

    @POST("project/member/{id}")
    suspend fun createProjectMember(
        @Path("id") projectId: String,
        @Body body: CreateProjectMemberRequest
    ): Response<CreateProjectMemberResponse>

    @GET("project/member/{id}")
    suspend fun getProjectMembers(
        @Path("id") projectId: String
    ): Response<GetProjectMemberResponse>


//    Parameter type must not include a type variable or wildcard: java.util.List<? extends okhttp3.RequestBody> (parameter #7)
//    for method ProjectRepositoryService.createProject
}