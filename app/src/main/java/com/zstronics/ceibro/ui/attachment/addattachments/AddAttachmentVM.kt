package com.zstronics.ceibro.ui.attachment.addattachments

import android.content.Context
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.local.FileAttachmentsDataSource
import com.zstronics.ceibro.data.repos.dashboard.IDashboardRepository
import com.zstronics.ceibro.data.repos.dashboard.attachment.AttachmentModules
import com.zstronics.ceibro.data.repos.dashboard.attachment.AttachmentUploadRequest
import com.zstronics.ceibro.utils.FileUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AddAttachmentVM @Inject constructor(
    override val viewState: AddAttachmentState,
    private val dashboardRepository: IDashboardRepository,
    private val fileAttachmentsDataSource: FileAttachmentsDataSource
) : HiltBaseViewModel<IAddAttachment.State>(), IAddAttachment.ViewModel {

    fun uploadFiles(module: String, id: String, context: Context) {
        loading(true)
        val moduleName = when (module) {
            "Task" -> AttachmentModules.Task
            "SubTask" -> AttachmentModules.SubTask
            "SubTaskComments" -> AttachmentModules.SubTaskComments
            "Project" -> AttachmentModules.Project
            else -> AttachmentModules.Task
        }

        val fileUriList = fileUriList.value
        val attachmentUriList = fileUriList?.map {
            FileUtils.getFile(
                context,
                it?.attachmentUri
            )
        }
        val request = AttachmentUploadRequest(
            _id = id,
            moduleName = moduleName,
            files = attachmentUriList
        )
        launch {
            when (val response = dashboardRepository.uploadFiles(request)) {
                is ApiResponse.Success -> {
                    val allFiles = response.data.results.files
                    val updatedFiles = allFiles.mapIndexed { index, file ->
                        if (fileUriList != null && fileUriList.size > index) {
                            file.copy(fileUrl = fileUriList[index]?.attachmentUri.toString())
                        } else {
                            file // return the original file if no URI is available at the corresponding index
                        }
                    }
                    fileAttachmentsDataSource.insertAll(updatedFiles)
                    handlePressOnView(1)
                    loading(false)
                }
                is ApiResponse.Error -> {
                    loading(false, response.error.message)
                    handlePressOnView(1)
                }
            }
        }
    }
}