package com.zstronics.ceibro.data.repos.projects

import com.zstronics.ceibro.data.repos.projects.createNewProject.CreateNewProjectResponse
import com.zstronics.ceibro.data.repos.projects.group.CreateProjectGroupResponse
import com.zstronics.ceibro.data.repos.projects.group.GetProjectGroupsResponse
import com.zstronics.ceibro.data.repos.projects.projectsmain.AllProjectsResponse
import com.zstronics.ceibro.data.repos.projects.projectsmain.ProjectMembersResponse
import com.zstronics.ceibro.data.repos.projects.projectsmain.ProjectsWithMembersResponse
import com.zstronics.ceibro.data.repos.projects.role.ProjectRolesResponse
import com.zstronics.ceibro.ui.projects.newproject.group.addnewgroup.CreateGroupRequest
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

    @GET("project/group/{id}")
    suspend fun getGroups(@Path("id") projectId: String): Response<GetProjectGroupsResponse>

    @GET("project/role/{id}")
    suspend fun getRoles(@Path("id") projectId: String): Response<ProjectRolesResponse>

//    Parameter type must not include a type variable or wildcard: java.util.List<? extends okhttp3.RequestBody> (parameter #7)
//    for method ProjectRepositoryService.createProject
}