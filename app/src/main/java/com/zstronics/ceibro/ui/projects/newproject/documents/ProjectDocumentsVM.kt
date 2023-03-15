package com.zstronics.ceibro.ui.projects.newproject.documents

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.database.models.attachments.FilesAttachments
import com.zstronics.ceibro.data.repos.chat.room.Member
import com.zstronics.ceibro.data.repos.dashboard.IDashboardRepository
import com.zstronics.ceibro.data.repos.dashboard.attachment.AttachmentModules
import com.zstronics.ceibro.data.repos.projects.IProjectRepository
import com.zstronics.ceibro.data.repos.projects.documents.CreateProjectFolderResponse
import com.zstronics.ceibro.data.repos.projects.projectsmain.ProjectsWithMembersResponse
import com.zstronics.ceibro.data.sessions.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProjectDocumentsVM @Inject constructor(
    override val viewState: ProjectDocumentsState,
    val sessionManager: SessionManager,
    private val projectRepository: IProjectRepository,
    private val dashboardRepository: IDashboardRepository
) : HiltBaseViewModel<IProjectDocuments.State>(), IProjectDocuments.ViewModel {

    private val _files: MutableLiveData<ArrayList<FilesAttachments>> =
        MutableLiveData(arrayListOf())
    val files: LiveData<ArrayList<FilesAttachments>> = _files

    private val _folders: MutableLiveData<ArrayList<CreateProjectFolderResponse.ProjectFolder>> =
        MutableLiveData(arrayListOf())
    val folders: LiveData<ArrayList<CreateProjectFolderResponse.ProjectFolder>> = _folders

    private val _projectMembers: MutableLiveData<List<Member?>> = MutableLiveData(arrayListOf())
    val projectMembers: LiveData<List<Member?>> = _projectMembers

    private val _projectGroups: MutableLiveData<List<ProjectsWithMembersResponse.ProjectDetail.Group>> =
        MutableLiveData(arrayListOf())
    val projectGroups: LiveData<List<ProjectsWithMembersResponse.ProjectDetail.Group>> =
        _projectGroups

    var isRootSelected = true
    var selectedFolderId = ""
    var projectId = ""
    fun documents(projectId: String) {
        getGroups(projectId)
        getMembers(projectId)
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

    fun uploadDocumentsInProject(context: Context) {
        val moduleId = if (isRootSelected) projectId else selectedFolderId
        val module =
            if (isRootSelected) AttachmentModules.Project.name else AttachmentModules.ProjectFolder.name
        val files = fileUriList.value?.map {
            FilesAttachments(
                id = "",
                access = listOf(),
                createdAt = "",
                fileName = it?.fileName.toString(),
                fileType = "",
                fileUrl = it?.attachmentUri.toString(),
                moduleId = moduleId,
                moduleType = module,
                updatedAt = "",
                uploadStatus = "pending",
                uploadedBy = "",
                version = 1,
                fileSize = it?.fileSize ?: 0
            )
        }
        if (!isRootSelected) {
            if (files != null) {
                patchFilesWithFolder(moduleId, files)
            }
        } else {
            if (files != null)
                patchFilesToRoot(files)
        }
        uploadFiles(module, moduleId, context)
    }

    fun getFilesUnderFolder(folderId: String) {
        launch {
            loading(true)
            val response = dashboardRepository.getFilesByModuleId(
                AttachmentModules.ProjectFolder.name,
                folderId
            )
            when (response) {
                is ApiResponse.Success -> {
                    loading(false)
                    response.data.results?.let { patchFilesWithFolder(folderId, it) }
                }
                is ApiResponse.Error -> {
                    loading(true, response.error.message)
                }
            }
        }
    }

    private fun patchFilesWithFolder(folderId: String, result: List<FilesAttachments>) {
        val folders = folders.value
        val folder = folders?.find { it.id == folderId }
        if (folder !== null) {
            val index = folders.indexOf(folder)
            if (index > -1) {
                folders.removeAt(index)
                if (folder.files == null)
                    folder.files = result as ArrayList<FilesAttachments>
                else
                    folder.files?.addAll(result)
                folders.add(index, folder)
                folders.let {
                    _folders.postValue(it)
                }

            }
        }
    }

    private fun patchFilesToRoot(result: List<FilesAttachments>) {
        var files = files.value
        if (files == null)
            files = result as ArrayList<FilesAttachments>
        else
            files.addAll(result)
        files.let {
            _files.postValue(it)
        }
    }

    private fun getGroups(id: String?) {
        launch {
            when (val response = projectRepository.getGroups(id ?: "")) {
                is ApiResponse.Success -> {
                    val groups = response.data.result.map {
                        ProjectsWithMembersResponse.ProjectDetail.Group(
                            it.id, it.members, it.name
                        )
                    }
                    _projectGroups.postValue(groups)
                }
                is ApiResponse.Error -> {
                    alert(response.error.message)
                }
            }
        }
    }

    private fun getMembers(id: String?) {
        launch {
            when (val response = projectRepository.getProjectMembers(id ?: "")) {
                is ApiResponse.Success -> {
                    val pureMembers = response.data.members.filter { it.user != null }.map {
                        it.user
                    }
                    _projectMembers.postValue(pureMembers)
                }
                is ApiResponse.Error -> {
                    alert(response.error.message)
                }
            }
        }
    }
}