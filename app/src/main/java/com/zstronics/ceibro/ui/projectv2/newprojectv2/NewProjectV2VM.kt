package com.zstronics.ceibro.ui.projectv2.newprojectv2


import android.content.Context
import android.net.Uri
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.database.dao.ProjectsV2Dao
import com.zstronics.ceibro.data.repos.projects.IProjectRepository
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.utils.FileUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject

@HiltViewModel
class NewProjectV2VM @Inject constructor(
    override val viewState: NewProjectStateV2,
    private val projectRepository: IProjectRepository,
    private val sessionManager: SessionManager,
    private val projectDao: ProjectsV2Dao,
) : HiltBaseViewModel<INewProjectV2.State>(), INewProjectV2.ViewModel {
    val user = sessionManager.getUser().value


    override fun getProjectName(context: Context) {

    }

    override fun addNewProject(context: Context, callBack: (isSuccess: Boolean) -> Unit) {

        val projectName = viewState.projectName.value
        val projectDescription = viewState.projectDescription.value ?: ""
        val projectPhoto = viewState.projectPhoto.value
        var projectPhotoFile: File? = null
        if (!projectPhoto.isNullOrEmpty()) {
            projectPhotoFile = FileUtils.getFile(context, Uri.parse(projectPhoto))
        }

        if (projectName.isNullOrEmpty()) {
            alert(context.getString(R.string.project_name_is_required))
        } else {
            val title = projectName.toRequestBody("text/plain".toMediaTypeOrNull())
            val description = projectDescription.toRequestBody("text/plain".toMediaTypeOrNull())

            if (projectPhotoFile != null) {
                val reqFile =
                    projectPhotoFile.asRequestBody(("image/" + projectPhotoFile.extension).toMediaTypeOrNull())
                val fileParts = MultipartBody.Part.createFormData("file", projectPhotoFile.name, reqFile)

                launch {
                    loading(true)
                    when (val response = projectRepository.createNewProjectWithFile(
                        title = title,
                        description = description,
                        file = fileParts
                    )) {
                        is ApiResponse.Success -> {
                            updateCreatedProjectInLocal(response.data.newProject, projectDao)
                            loading(false, "Project created successfully")
                            callBack(true)
                        }

                        is ApiResponse.Error -> {
                            loading(false, response.error.message)
                            callBack(false)
                        }
                    }
                }
            } else {
                launch {
                    loading(true)
                    when (val response = projectRepository.createNewProjectWithoutFile(
                        title = title,
                        description = description
                    )) {
                        is ApiResponse.Success -> {
                            updateCreatedProjectInLocal(response.data.newProject, projectDao)
                            loading(false, "Project created successfully")
                            callBack(true)
                        }

                        is ApiResponse.Error -> {
                            loading(false, response.error.message)
                            callBack(false)
                        }
                    }
                }
            }

        }
    }


}
