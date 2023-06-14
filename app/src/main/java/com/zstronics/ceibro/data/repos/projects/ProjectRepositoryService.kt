package com.zstronics.ceibro.data.repos.projects

import com.zstronics.ceibro.data.base.BaseResponse
import com.zstronics.ceibro.data.repos.projects.createNewProject.CreateNewProjectResponse
import com.zstronics.ceibro.data.repos.projects.createNewProject.UpdateProjectResponse
import com.zstronics.ceibro.data.repos.projects.documents.CreateProjectFolderRequest
import com.zstronics.ceibro.data.repos.projects.documents.CreateProjectFolderResponse
import com.zstronics.ceibro.data.repos.projects.documents.ManageProjectDocumentAccessRequest
import com.zstronics.ceibro.data.repos.projects.documents.ProjectDocumentsResponse
import com.zstronics.ceibro.data.repos.projects.group.CreateGroupRequest
import com.zstronics.ceibro.data.repos.projects.group.CreateProjectGroupResponse
import com.zstronics.ceibro.data.repos.projects.group.GetProjectGroupsResponse
import com.zstronics.ceibro.data.repos.projects.member.*
import com.zstronics.ceibro.data.repos.projects.projectsmain.AllProjectsResponse
import com.zstronics.ceibro.data.repos.projects.projectsmain.AllProjectsResponseV2
import com.zstronics.ceibro.data.repos.projects.projectsmain.GetAvailableMemberResponse
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
    @GET("v1/project")
    suspend fun getProjects(): Response<AllProjectsResponse>

    @GET("v2/project")
    suspend fun getProjectsV2(): Response<AllProjectsResponseV2>

    @POST("v1/project/getProjectsWithMembers")
    suspend fun getProjectsWithMembers(@Query("includeMe") includeMe: Boolean = false): Response<ProjectsWithMembersResponse>


    @Multipart
    @POST("v1/project")
    suspend fun createProject(
        @Part projectPhoto: MultipartBody.Part?,
        @Part("title") title: RequestBody,
        @Part("location") location: RequestBody,
        @Part("description") description: RequestBody,
        @Part("dueDate") dueDate: RequestBody,
        @Part("publishStatus") publishStatus: RequestBody,
        @Part("extraStatus") extraStatus: RequestBody,
        @Part("owner") owner: RequestBody
    ): Response<CreateNewProjectResponse>

    @Multipart
    @PATCH("v1/project/{projectId}")
    suspend fun updateProject(
        @Path("projectId") projectId: String,
        @Part projectPhoto: MultipartBody.Part?,
        @Part("title") title: RequestBody?,
        @Part("location") location: RequestBody?,
        @Part("description") description: RequestBody?,
        @Part("dueDate") dueDate: RequestBody?,
        @Part("publishStatus") publishStatus: RequestBody?,
        @Part("extraStatus") extraStatus: RequestBody?,
        @Part("owner") owner: RequestBody?
    ): Response<UpdateProjectResponse>

    @Multipart
    @PATCH("v1/project/{projectId}")
    suspend fun updateProject(
        @Path("projectId") projectId: String,
        @Part("title") title: RequestBody?,
        @Part("location") location: RequestBody?,
        @Part("description") description: RequestBody?,
        @Part("dueDate") dueDate: RequestBody?,
        @Part("publishStatus") publishStatus: RequestBody?,
        @Part("extraStatus") extraStatus: RequestBody?,
        @Part("owner") owner: RequestBody?
    ): Response<UpdateProjectResponse>

    @POST("v1/project/group/{id}")
    suspend fun createGroup(
        @Path("id") projectId: String,
        @Body body: CreateGroupRequest
    ): Response<CreateProjectGroupResponse>

    @PATCH("v1/project/group/{id}")
    suspend fun updateGroup(
        @Path("id") groupId: String,
        @Body body: CreateGroupRequest
    ): Response<CreateProjectGroupResponse>

    @DELETE("v1/project/group/{id}")
    suspend fun deleteGroup(@Path("id") id: String): Response<BaseResponse>

    @GET("v1/project/group/{id}")
    suspend fun getGroups(@Path("id") projectId: String): Response<GetProjectGroupsResponse>

    @GET("v1/project/role/{id}")
    suspend fun getRoles(@Path("id") projectId: String): Response<ProjectRolesResponse>

    @POST("v1/project/role/{id}")
    suspend fun createRoles(
        @Path("id") projectId: String,
        @Body body: CreateRoleRequest
    ): Response<CreateRoleResponse>

    @PATCH("v1/project/role/{id}")
    suspend fun updateRoles(
        @Path("id") projectId: String,
        @Body body: CreateRoleRequest
    ): Response<CreateRoleResponse>

    @DELETE("v1/project/role/{id}")
    suspend fun deleteRole(@Path("id") roleId: String): Response<BaseResponse>

    @POST("v1/project/member/{id}")
    suspend fun createProjectMember(
        @Path("id") projectId: String,
        @Body body: CreateProjectMemberRequest
    ): Response<CreateProjectMemberResponse>

    @DELETE("v1/project/member/remove/{id}")
    suspend fun deleteProjectMember(
        @Path("id") memberId: String
    ): Response<CreateProjectMemberResponse>

    @GET("v1/project/member/{id}")
    suspend fun getProjectMembers(
        @Path("id") projectId: String
    ): Response<GetProjectMemberResponse>

    @GET("v1/project/members/available/{id}")
    suspend fun getAvailableMembers(
        @Path("id") projectId: String
    ): Response<GetAvailableMemberResponse>

    @DELETE("v1/project/member/remove/{id}")
    suspend fun deleteMember(
        @Path("id") id: String
    ): Response<DeleteMemberResponse>

    @PATCH("v1/project/member/update/{memberId}")
    suspend fun updateProjectMember(
        @Path("memberId") memberId: String,
        @Body body: EditProjectMemberRequest
    ): Response<EditProjectMemberResponse>

    @POST("v1/project/folder/{id}")
    suspend fun createProjectFolder(
        @Path("id") projectId: String,
        @Body body: CreateProjectFolderRequest
    ): Response<CreateProjectFolderResponse>

    @GET("v1/project/documents/{id}")
    suspend fun getProjectDocuments(
        @Path("id") projectId: String
    ): Response<ProjectDocumentsResponse>

    @PATCH("v1/project/documents/updateAccess/{id}")
    suspend fun updateDocumentAccess(
        @Path("id") fileOrFolderId: String,
        @Body body: ManageProjectDocumentAccessRequest
    ): Response<BaseResponse>

//    Parameter type must not include a type variable or wildcard: java.util.List<? extends okhttp3.RequestBody> (parameter #7)
//    for method ProjectRepositoryService.createProject
}