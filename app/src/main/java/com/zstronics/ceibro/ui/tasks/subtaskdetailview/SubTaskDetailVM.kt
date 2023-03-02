package com.zstronics.ceibro.ui.tasks.subtaskdetailview

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.database.models.attachments.FilesAttachments
import com.zstronics.ceibro.data.database.models.subtask.AllSubtask
import com.zstronics.ceibro.data.database.models.subtask.SubTaskComments
import com.zstronics.ceibro.data.database.models.tasks.TaskMember
import com.zstronics.ceibro.data.local.FileAttachmentsDataSource
import com.zstronics.ceibro.data.repos.auth.login.User
import com.zstronics.ceibro.data.repos.dashboard.IDashboardRepository
import com.zstronics.ceibro.data.repos.dashboard.attachment.AttachmentModules
import com.zstronics.ceibro.data.repos.task.ITaskRepository
import com.zstronics.ceibro.data.repos.task.models.SubtaskCommentRequest
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.ui.attachment.AttachmentTypes
import com.zstronics.ceibro.ui.attachment.SubtaskAttachment
import com.zstronics.ceibro.ui.tasks.task.TaskStatus
import com.zstronics.ceibro.utils.DateUtils
import com.zstronics.ceibro.utils.FileUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SubTaskDetailVM @Inject constructor(
    override val viewState: SubTaskDetailState,
    val sessionManager: SessionManager,
    private val fileAttachmentsDataSource: FileAttachmentsDataSource,
    private val dashboardRepository: IDashboardRepository,
    private val taskRepository: ITaskRepository
) : HiltBaseViewModel<ISubTaskDetail.State>(), ISubTaskDetail.ViewModel {
    private val userObj = sessionManager.getUser().value

    private val _user: MutableLiveData<User?> = MutableLiveData()
    val user: LiveData<User?> = _user

    private val _subtask: MutableLiveData<AllSubtask?> = MutableLiveData()
    val subtask: LiveData<AllSubtask?> = _subtask


    private val _recentComments: MutableLiveData<ArrayList<SubTaskComments>> = MutableLiveData()
    val recentComments: LiveData<ArrayList<SubTaskComments>> = _recentComments
    override fun onFirsTimeUiCreate(bundle: Bundle?) {
        super.onFirsTimeUiCreate(bundle)
        _user.value = userObj

        val subtaskParcel: AllSubtask? = bundle?.getParcelable("subtask")
        _subtask.value = subtaskParcel
        subtaskParcel?.let {
            it.recentComments?.let { comments -> composeCommentsList(comments) }
            launch {
                when (val response =
                    dashboardRepository.getFilesByModuleId(module = "Task", moduleId = it.id)) {
                    is ApiResponse.Success -> {
                        response.data.results?.let { it1 -> fileAttachmentsDataSource.insertAll(it1) }
                    }
                    else -> {}
                }
            }
        }
    }

    private fun composeCommentsList(recentComments: ArrayList<SubTaskComments>) {
        _recentComments.value = recentComments
    }

    fun postComment(
        message: String,
        context: Context,
        success: (data: SubTaskComments?) -> Unit
    ) {
        launch {
            val userState =
                subtask.value?.state?.find { it.userId == userObj?.id }?.userState?.lowercase()
                    ?: TaskStatus.DRAFT.name.lowercase()
            val request = SubtaskCommentRequest(
                isFileAttached = fileUriList.value?.isNotEmpty() == true,
                message = message,
                seenBy = arrayListOf(userObj?.id.toString()),
                sender = userObj?.id,
                subTaskId = subtask.value?.id,
                taskId = subtask.value?.taskId,
                userState = userState
            )
            addComment(context, request, fileUriList.value)
            taskRepository.postCommentSubtask(request) { isSuccess, error, commentData ->
                loading(false)
                if (isSuccess) {
                    alert("Comment sent")
                    success(commentData)
                    if (fileUriList.value?.isNotEmpty() == true) {
                        commentData?.id?.let {
                            uploadFiles(
                                AttachmentModules.SubTaskComments.name,
                                it,
                                context
                            )
                        }
                    }
                } else alert(error)
            }
        }
    }

    private fun addComment(
        context: Context,
        request: SubtaskCommentRequest,
        commentsAttachments: ArrayList<SubtaskAttachment?>?
    ) {
        val files = try {
            commentsAttachments?.map {

                val mimeType = FileUtils.getMimeType(context, it?.attachmentUri)
                val fileName = FileUtils.getFileName(context, it?.attachmentUri)
                val fileSize = FileUtils.getFileSizeInBytes(context, it?.attachmentUri)
                val fileSizeReadAble = FileUtils.getReadableFileSize(fileSize)
                val attachmentType = when {
                    mimeType.startsWith("image") -> {
                        AttachmentTypes.Image
                    }
                    mimeType.startsWith("video") -> {
                        AttachmentTypes.Video
                    }
                    mimeType == "application/pdf" -> {
                        AttachmentTypes.Pdf
                    }
                    mimeType == "application/msword" || mimeType == "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> {
                        AttachmentTypes.Doc
                    }
                    else -> AttachmentTypes.Doc
                }

                FilesAttachments(
                    id = "ABCHD67",
                    access = listOf(),
                    createdAt = DateUtils.getCurrentDateWithFormat(DateUtils.SERVER_DATE_FULL_FORMAT),
                    updatedAt = DateUtils.getCurrentDateWithFormat(DateUtils.SERVER_DATE_FULL_FORMAT),
                    fileName = fileName,
                    fileType = attachmentType.name,
                    fileUrl = it?.attachmentUri.toString(),
                    moduleId = "",
                    moduleType = AttachmentModules.SubTaskComments.name,
                    uploadStatus = "inprogress",
                    uploadedBy = userObj?.id.toString(),
                    version = 1,
                    fileSize = fileSize
                )
            } as ArrayList<FilesAttachments>?
        } catch (e: Exception) {
            arrayListOf()
        }
        val comments = _recentComments.value ?: arrayListOf()
        val sender = TaskMember(
            TaskMemberId = 0,
            firstName = userObj?.firstName.toString(),
            surName = userObj?.surName.toString(),
            profilePic = userObj?.profilePic.toString(),
            id = userObj?.id.toString()
        )
        val comment = SubTaskComments(
            id = "121BJO",
            access = subtask.value?.access ?: listOf(),
            createdAt = DateUtils.getCurrentDateWithFormat(DateUtils.SERVER_DATE_FULL_FORMAT),
            updatedAt = DateUtils.getCurrentDateWithFormat(DateUtils.SERVER_DATE_FULL_FORMAT),
            isFileAttached = request.isFileAttached ?: false,
            message = request.message.toString(),
            seenBy = null,
            sender = sender,
            subTaskId = request.subTaskId.toString(),
            taskId = request.taskId.toString(),
            userState = request.userState.toString(),
            files = files
        )
        comments.add(comment)
        _recentComments.postValue(comments)
    }

}