package com.zstronics.ceibro.data.repos.projects

import com.zstronics.ceibro.data.repos.projects.projectsmain.AllProjectsResponse
import com.zstronics.ceibro.data.repos.projects.projectsmain.ProjectMembersResponse
import com.zstronics.ceibro.data.repos.projects.projectsmain.ProjectsWithMembersResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ProjectRepositoryService {
    @GET("project")
    suspend fun getProjects(@Query("publishStatus") publishStatus: String = "all"): Response<AllProjectsResponse>

    @POST("project/getProjectsWithMembers")
    suspend fun getProjectsWithMembers(@Query("includeMe") includeMe: Boolean = false): Response<ProjectsWithMembersResponse>

    @GET("project/member/{id}")
    suspend fun getMemberByProjectId(@Path("id") projectId: String): Response<ProjectMembersResponse>
}