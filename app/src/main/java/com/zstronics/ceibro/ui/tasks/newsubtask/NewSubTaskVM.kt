package com.zstronics.ceibro.ui.tasks.newsubtask

import android.content.Context
import android.os.Bundle
import android.os.Handler
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.database.models.subtask.AllSubtask
import com.zstronics.ceibro.data.database.models.tasks.CeibroTask
import com.zstronics.ceibro.data.database.models.tasks.TaskMember
import com.zstronics.ceibro.data.local.FileAttachmentsDataSource
import com.zstronics.ceibro.data.repos.chat.room.Member
import com.zstronics.ceibro.data.repos.dashboard.IDashboardRepository
import com.zstronics.ceibro.data.repos.dashboard.attachment.AttachmentModules
import com.zstronics.ceibro.data.repos.projects.IProjectRepository
import com.zstronics.ceibro.data.repos.task.ITaskRepository
import com.zstronics.ceibro.data.repos.task.models.NewSubtaskRequest
import com.zstronics.ceibro.data.repos.task.models.UpdateDraftSubtaskRequest
import com.zstronics.ceibro.data.repos.task.models.UpdateSubtaskRequest
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.ui.socket.LocalEvents
import com.zstronics.ceibro.ui.tasks.subtask.SubTaskStatus
import com.zstronics.ceibro.ui.tasks.task.TaskStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject

@HiltViewModel
class NewSubTaskVM @Inject constructor(
    override val viewState: NewSubTaskState,
    val sessionManager: SessionManager,
    private val projectRepository: IProjectRepository,
    private val taskRepository: ITaskRepository
) : HiltBaseViewModel<INewSubTask.State>(), INewSubTask.ViewModel {
    val user = sessionManager.getUser().value
    var isNewSubTask = true
    var subtaskId = ""

    private val _task: MutableLiveData<CeibroTask?> = MutableLiveData()
    val task: LiveData<CeibroTask?> = _task

    private val _subtask: MutableLiveData<AllSubtask> = MutableLiveData()
    val subtask: LiveData<AllSubtask> = _subtask

    private val _taskAssignee: MutableLiveData<ArrayList<Member>?> = MutableLiveData(arrayListOf())
    val taskAssignee: MutableLiveData<ArrayList<Member>?> = _taskAssignee

//    private val _viewers: MutableLiveData<ArrayList<Member>> = MutableLiveData(arrayListOf())
//    val viewers: MutableLiveData<ArrayList<Member>> = _viewers

    private val _projectMembers: MutableLiveData<List<Member>> = MutableLiveData(arrayListOf())
    val projectMembers: LiveData<List<Member>> = _projectMembers

    private val _projectMemberNames: MutableLiveData<List<String>> = MutableLiveData(arrayListOf())
    val projectMemberNames: LiveData<List<String>> = _projectMemberNames

    override fun onFirsTimeUiCreate(bundle: Bundle?) {
        super.onFirsTimeUiCreate(bundle)
        val isNSubTask: Boolean = bundle?.getBoolean("newSubTask") ?: true
        isNewSubTask = isNSubTask
        val taskParcel: CeibroTask? = bundle?.getParcelable("task")
        val subtaskParcel: AllSubtask? = bundle?.getParcelable("subtask")

        if (isNSubTask) {
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
            _task.value = taskParcel

            taskParcel?.project?.id?.let { loadMemberByProjectId(it) }
        } else {
            _task.value = taskParcel
            subtaskParcel?.taskData?.project?.id?.let {
                loadMemberByProjectIdBySubTask(
                    it,
                    subtaskParcel
                )
            }
        }
    }

    private fun loadMemberByProjectId(projectId: String) {
        launch {
            loading(true)
            when (val response = projectRepository.getProjectMembers(projectId)) {
                is ApiResponse.Success -> {
                    val onlyMembers: List<Member> = response.data.members.filter { it.user != null }.map {
                        it.user!!
                    }
                    onlyMembers.let { members ->
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

    private fun loadMemberByProjectIdBySubTask(projectId: String, subtaskParcel: AllSubtask) {
        launch {
            loading(true)
            when (val response = projectRepository.getProjectMembers(projectId)) {
                is ApiResponse.Success -> {
                    val onlyMembers: List<Member> = response.data.members.filter { it.user != null }.map {
                        it.user!!
                    }
                    onlyMembers.let { members ->
                        _projectMembers.postValue(members)
                        _projectMemberNames.postValue(members.map { it.firstName + " " + it.surName })
                    }
                    loading(false)
                    val handler = Handler()
                    handler.postDelayed(Runnable {
                        setEditSubTaskDetails(subtaskParcel)
                    }, 50)
                }

                is ApiResponse.Error -> {
                    loading(false)
                }
            }
        }
    }

    private fun setEditSubTaskDetails(subtaskParcel: AllSubtask) {
        subtaskParcel.let {
            subtaskId = subtaskParcel.id
            _subtask.value = subtaskParcel

            val members: ArrayList<TaskMember> = ArrayList()
            for (assign in subtaskParcel.assignedTo) {
                for (member in assign.members) {
                    members.add(member)
                }
            }

            val assigneeList = members.map {
                Member(
                    companyName = "",
                    firstName = it.firstName,
                    surName = it.surName,
                    id = it.id,
                    profilePic = it.profilePic
                )
            }
            _taskAssignee.value = assigneeList as ArrayList<Member>?
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

    fun createNewSubTask(
        state: String,
        context: Context,
        success: (subtaskId: String) -> Unit,
        back: () -> Unit
    ) {
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
        var highestState = if (state == SubTaskStatus.DRAFT.name.lowercase()) {
            SubTaskStatus.DRAFT.name.lowercase()
        } else {
            SubTaskStatus.ASSIGNED.name.lowercase()
        }

        val assigneeStates = if (state == SubTaskStatus.DRAFT.name.lowercase()) {
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
                        highestState = SubTaskStatus.ACCEPTED.name.lowercase()
                        highestState
                    } else state
                )
            }
        }

        val adminsStates = adminsId?.map { id ->
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
            taskRepository.newSubTask(newTaskRequest) { isSuccess, error, subtaskData ->
                if (isSuccess) {
                    if (fileUriList.value?.isNotEmpty() == true) {
                        subtaskData?.id?.let {
                            success.invoke(it)
                            uploadFiles(
                                AttachmentModules.SubTask.name,
                                it,
                                context
                            )
                        }
                    }
                    back.invoke()
                    loading(false, "")
                } else {
                    loading(false, error)
                }
            }
        }
    }


    fun updateDraftSubTask(subTaskId: String, state: String, back: () -> Unit) {
        val assigneeMembersId = taskAssignee.value?.map { it.id }
        val assignedTo: List<UpdateDraftSubtaskRequest.AssignedTo> = listOf(
            UpdateDraftSubtaskRequest.AssignedTo(
                addedBy = user?.id,
                members = assigneeMembersId
            )
        )

        val adminsId = subtask.value?.taskData?.admins?.map { it }

        var highestState = if (state == SubTaskStatus.DRAFT.name.lowercase()) {
            SubTaskStatus.DRAFT.name.lowercase()
        } else {
            SubTaskStatus.ASSIGNED.name.lowercase()
        }


        val assigneeStates = if (state == SubTaskStatus.DRAFT.name.lowercase()) {
            listOf(
                UpdateDraftSubtaskRequest.State(
                    userId = user?.id,
                    userState = state
                )
            )
        } else {
            assigneeMembersId?.map { id ->
                UpdateDraftSubtaskRequest.State(
                    userId = id,
                    userState = if (isAdmin(id)) {
                        highestState = SubTaskStatus.ACCEPTED.name.lowercase()
                        highestState
                    } else state
                )
            }
        }

        val adminsStates = adminsId?.map { id ->
            UpdateDraftSubtaskRequest.State(
                userId = id,
                userState = highestState
            )
        }
        val finalStates = arrayListOf<UpdateDraftSubtaskRequest.State>()

        if (assigneeStates != null) {
            finalStates.addAll(assigneeStates)
        }
        if (adminsStates != null) {
            finalStates.addAll(adminsStates)
        }
        val states = finalStates.distinctBy { it.userId }

        val updateDraftSubtask = UpdateDraftSubtaskRequest(
            assignedTo = assignedTo,
            description = viewState.description.value.toString(),
            dueDate = viewState.dueDate,
            state = states,
            title = viewState.subtaskTitle.value.toString()
        )

        launch {
            loading(true)
            taskRepository.updateSubTaskById(subTaskId, updateDraftSubtask) { isSuccess, error ->
                if (isSuccess) {
                    loading(false, "SubTask Updated Successfully")
                    back.invoke()
                } else {
                    loading(false, error)
                }
            }
        }
    }


    fun updateAssignedSubTask(subTaskId: String, back: () -> Unit) {
        val updateSubtask = UpdateSubtaskRequest(
            description = viewState.description.value.toString()
        )

        launch {
            loading(true)
            taskRepository.updateSubTask(subTaskId, updateSubtask) { isSuccess, error ->
                if (isSuccess) {
                    loading(false, "SubTask Updated Successfully")
                    back.invoke()
                } else {
                    loading(false, error)
                }
            }
        }
    }


    private fun isAdmin(id: String): Boolean {
        return id == user?.id
    }
}