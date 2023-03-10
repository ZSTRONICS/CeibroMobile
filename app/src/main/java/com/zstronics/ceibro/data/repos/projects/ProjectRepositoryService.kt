package com.zstronics.ceibro.data.repos.projects

import com.zstronics.ceibro.data.repos.projects.createNewProject.CreateNewProjectResponse
import com.zstronics.ceibro.data.repos.projects.createNewProject.CreateProjectRequest
import com.zstronics.ceibro.data.repos.projects.projectsmain.AllProjectsResponse
import com.zstronics.ceibro.data.repos.projects.projectsmain.ProjectMembersResponse
import com.zstronics.ceibro.data.repos.projects.projectsmain.ProjectsWithMembersResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ProjectRepositoryService {
    @GET("project")
    suspend fun getProjects(@Query("publishStatus") publishStatus: String = "all"): Response<AllProjectsResponse>

    @POST("project/getProjectsWithMembers")
    suspend fun getProjectsWithMembers(@Query("includeMe") includeMe: Boolean = false): Response<ProjectsWithMembersResponse>

    @GET("project/member/{id}")
    suspend fun getMemberByProjectId(@Path("id") projectId: String): Response<ProjectMembersResponse>

    @Multipart
    @POST("project")
    suspend fun createProject(@Body request: CreateProjectRequest): Response<CreateNewProjectResponse>

//    Parameter type must not include a type variable or wildcard: java.util.List<? extends okhttp3.RequestBody> (parameter #7)
//    for method ProjectRepositoryService.createProject
}