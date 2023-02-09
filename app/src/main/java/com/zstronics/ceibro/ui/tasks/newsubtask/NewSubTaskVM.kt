package com.zstronics.ceibro.ui.tasks.newsubtask

import android.net.Uri
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.database.models.tasks.CeibroTask
import com.zstronics.ceibro.data.database.models.tasks.TaskMember
import com.zstronics.ceibro.data.repos.chat.room.Member
import com.zstronics.ceibro.data.repos.projects.IProjectRepository
import com.zstronics.ceibro.data.repos.projects.projectsmain.ProjectsWithMembersResponse
import com.zstronics.ceibro.data.repos.task.ITaskRepository
import com.zstronics.ceibro.data.repos.task.models.NewSubtaskRequest
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.ui.tasks.task.TaskStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NewSubTaskVM @Inject constructor(
    override val viewState: NewSubTaskState,
    val sessionManager: SessionManager,
    private val projectRepository: IProjectRepository,
    private val taskRepository: ITaskRepository,
) : HiltBaseViewModel<INewSubTask.State>(), INewSubTask.ViewModel {
    val user = sessionManager.getUser().value

    private val _task: MutableLiveData<CeibroTask> = MutableLiveData()
    val task: LiveData<CeibroTask> = _task

    private val _taskAssignee: MutableLiveData<ArrayList<Member>> = MutableLiveData(arrayListOf())
    val taskAssignee: MutableLiveData<ArrayList<Member>> = _taskAssignee


    private val _fileUriList: MutableLiveData<ArrayList<SubtaskAttachment?>> =
        MutableLiveData(arrayListOf())
    val fileUriList: MutableLiveData<ArrayList<SubtaskAttachment?>> = _fileUriList

//    private val _viewers: MutableLiveData<ArrayList<Member>> = MutableLiveData(arrayListOf())
//    val viewers: MutableLiveData<ArrayList<Member>> = _viewers

    private val _projectMembers: MutableLiveData<List<Member>> = MutableLiveData(arrayListOf())
    val projectMembers: LiveData<List<Member>> = _projectMembers

    private val _projectMemberNames: MutableLiveData<List<String>> = MutableLiveData(arrayListOf())
    val projectMemberNames: LiveData<List<String>> = _projectMemberNames

    override fun onFirsTimeUiCreate(bundle: Bundle?) {
        super.onFirsTimeUiCreate(bundle)
        val taskParcel: CeibroTask? = bundle?.getParcelable("task")
        _task.value = taskParcel

        val list = taskParcel?.assignedTo?.map {
            Member(
                companyName = "",
                firstName = it.firstName,
                surName = it.surName,
                id = it.id,
                profilePic = it.profilePic
            )
        }
        _taskAssignee.value = list as ArrayList<Member>?
        taskParcel?.project?.id?.let { loadMemberByProjectId(it) }
    }

    private fun loadMemberByProjectId(projectId: String) {
        launch {
            loading(true)
            when (val response = projectRepository.getMemberByProjectId(projectId)) {
                is ApiResponse.Success -> {
                    response.data.members.let { members ->
                        _projectMembers.postValue(members)
                        _projectMemberNames.postValue(members.map { it.firstName + " " + it.surName })
                    }
                    loading(false)
                }

                is ApiResponse.Error -> {
                    loading(false)
                }
            }
        }
    }


    fun onAssigneeSelect(position: Int) {
        val member: Member? = projectMembers.value?.get(position)
        val assignees = _taskAssignee.value

        val selectedMember = assignees?.find { it.id == member?.id }

        if (selectedMember != null) {
            assignees.remove(selectedMember)
        } else {
            if (member != null) {
                assignees?.add(member)
            }
        }
        _taskAssignee.value = assignees
    }

//    fun onViewerSelect(position: Int) {
//        val member: Member? = projectMembers.value?.get(position)
//        val viewers = _viewers.value
//        if (viewers?.contains(member) == true) {
//            viewers.remove(member)
//        } else {
//            if (member != null) {
//                viewers?.add(member)
//            }
//        }
//        _viewers.value = viewers
//    }

    fun removeAssignee(data: Member) {
        val assignee = _taskAssignee.value
        assignee?.remove(data)
        _taskAssignee.value = assignee
    }

//    fun removeViewer(data: Member) {
//        val viewers = _viewers.value
//        viewers?.remove(data)
//        _viewers.value = viewers
//    }

    fun createNewSubTask(state: String) {
        val assigneeMembersId = taskAssignee.value?.map { it.id }
        val assignedTo: List<NewSubtaskRequest.AssignedTo> = listOf(
            NewSubtaskRequest.AssignedTo(
                addedBy = user?.id,
                members = assigneeMembersId
            )
        )

//        val viewersMembersId = viewers.value?.map { it.id }
//        val viewers: List<NewSubtaskRequest.Viewer> = listOf(
//            NewSubtaskRequest.Viewer(
//                addedBy = user?.id,
//                members = viewersMembersId
//            )
//        )
        val adminsId = task.value?.admins?.map { it.id }
        var highestState = TaskStatus.ASSIGNED.name.lowercase()

        val assigneeStates = if (state == TaskStatus.DRAFT.name.lowercase()) {
            listOf(
                NewSubtaskRequest.State(
                    userId = user?.id,
                    userState = state
                )
            )
        } else {
            assigneeMembersId?.map { id ->
                NewSubtaskRequest.State(
                    userId = id,
                    userState = if (isAdmin(id)) {
                        highestState = TaskStatus.ACCEPTED.name.lowercase()
                        highestState
                    } else state
                )
            }
        }

        var adminsStates = adminsId?.map { id ->
            NewSubtaskRequest.State(
                userId = id,
                userState = highestState
            )
        }
        val finalStates = arrayListOf<NewSubtaskRequest.State>()

        if (assigneeStates != null) {
            finalStates.addAll(assigneeStates)
        }
        if (adminsStates != null) {
            finalStates.addAll(adminsStates)
        }
        val states = finalStates.distinctBy { it.userId }

        val newTaskRequest = NewSubtaskRequest(
            assignedTo = assignedTo,
            creator = user?.id ?: "",
            description = viewState.description.value.toString(),
            doneCommentsRequired = viewState.doneCommentsRequired.value ?: false,
            doneImageRequired = viewState.doneImageRequired.value ?: false,
            dueDate = viewState.dueDate,
            isMultiTaskSubTask = false,
            state = states,
            taskId = task.value?._id ?: "",
            title = viewState.subtaskTitle.value.toString()
        )

        launch {
            loading(true)
            taskRepository.newSubTask(newTaskRequest) { isSuccess, error ->
                loading(false, error)
                if (isSuccess)
                    handlePressOnView(1)
            }
        }
    }

    private fun isAdmin(id: String): Boolean {
        return id == user?.id
    }

    fun addUriToList(data: SubtaskAttachment) {
        val files = fileUriList.value
        files?.add(data)
        _fileUriList.postValue(files)
    }

    fun removeFile(position: Int) {
        val files = fileUriList.value
        files?.removeAt(position)
        _fileUriList.postValue(files)
    }

    data class SubtaskAttachment(
        val attachmentType: String,
        val attachmentUri: Uri?
    )
}