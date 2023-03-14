package com.zstronics.ceibro.ui.projects.newproject.documents

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.database.models.attachments.FilesAttachments
import com.zstronics.ceibro.data.repos.dashboard.attachment.AttachmentModules
import com.zstronics.ceibro.data.repos.projects.IProjectRepository
import com.zstronics.ceibro.data.repos.projects.documents.CreateProjectFolderResponse
import com.zstronics.ceibro.data.sessions.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProjectDocumentsVM @Inject constructor(
    override val viewState: ProjectDocumentsState,
    val sessionManager: SessionManager,
    private val projectRepository: IProjectRepository
) : HiltBaseViewModel<IProjectDocuments.State>(), IProjectDocuments.ViewModel {

    private val _files: MutableLiveData<ArrayList<FilesAttachments>> =
        MutableLiveData(arrayListOf())
    val files: LiveData<ArrayList<FilesAttachments>> = _files

    private val _folders: MutableLiveData<ArrayList<CreateProjectFolderResponse.ProjectFolder>> =
        MutableLiveData(arrayListOf())
    val folders: LiveData<ArrayList<CreateProjectFolderResponse.ProjectFolder>> = _folders

    fun documents(projectId: String) {
        launch {
            when (val response = projectRepository.getProjectDocuments(projectId)) {
                is ApiResponse.Success -> {
                    val docs = response.data.documentResult
                    _files.postValue(docs.files as ArrayList<FilesAttachments>?)
                    _folders.postValue(docs.folders as ArrayList<CreateProjectFolderResponse.ProjectFolder>?)
                }
                is ApiResponse.Error -> {
                    alert(response.error.message)
                }
            }
        }
    }

    fun addFolder(projectId: String, folderName: String) {
        launch {
            when (val response = projectRepository.createProjectFolder(projectId, folderName)) {
                is ApiResponse.Success -> {
                    alert("Folder created $folderName")
                    val folders = folders.value
                    folders?.add(response.data.folder)
                    folders?.let {
                        _folders.postValue(it)
                    }
                }
                is ApiResponse.Error -> {

                }
            }
        }
    }

    fun uploadFiles(context: Context, moduleId: String) {
        uploadFiles(
            AttachmentModules.Project.name,
            moduleId,
            context
        )
    }
}