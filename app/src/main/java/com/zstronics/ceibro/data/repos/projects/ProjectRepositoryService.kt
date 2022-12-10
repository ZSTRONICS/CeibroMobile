package com.zstronics.ceibro.data.repos.projects

import com.zstronics.ceibro.data.repos.projects.projectsmain.AllProjectsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ProjectRepositoryService {
    @GET("project")
    suspend fun getProjects(@Query("publishStatus") publishStatus: String = "all"): Response<AllProjectsResponse>

    @GET("getProjectsWithMembers")
    suspend fun getProjectsWithMembers(): Response<AllProjectsResponse>
}