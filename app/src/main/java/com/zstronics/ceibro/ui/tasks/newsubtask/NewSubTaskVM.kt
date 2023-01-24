package com.zstronics.ceibro.ui.tasks.newsubtask

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.database.models.tasks.CeibroTask
import com.zstronics.ceibro.data.repos.chat.room.Member
import com.zstronics.ceibro.data.repos.projects.IProjectRepository
import com.zstronics.ceibro.data.repos.projects.projectsmain.ProjectsWithMembersResponse
import com.zstronics.ceibro.data.repos.task.ITaskRepository
import com.zstronics.ceibro.data.repos.task.models.NewSubtaskRequest
import com.zstronics.ceibro.data.sessions.SessionManager
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

        taskParcel?._id?.let { loadProjects(it) }
    }

    private fun loadProjects(projectId: String) {
        launch {
            loading(true)
            when (val response = projectRepository.getProjectsWithMembers(false)) {

                is ApiResponse.Success -> {
                    response.data.projectDetails.let { projects ->
                        if (projects.isNotEmpty()) {
//                            val selectedProject = projects.find { it.id == projectId }
                            val selectedProject = projects.first()
                            if (selectedProject !== null)
                                onProjectSelect(selectedProject)
                        }
                    }
                    loading(false)
                }

                is ApiResponse.Error -> {
                    loading(false)
                }
            }
        }
    }

    private fun onProjectSelect(selectedProject: ProjectsWithMembersResponse.ProjectDetail?) {
        val projectMemb = selectedProject?.projectMembers as MutableList
        _projectMembers.postValue(projectMemb)
        _projectMemberNames.postValue(projectMemb.map { it.firstName + " " + it.surName })
    }


    fun onAssigneeSelect(position: Int) {
        val member: Member? = projectMembers.value?.get(position)
        val assignees = _taskAssignee.value
        if (assignees?.contains(member) == true) {
            assignees.remove(member)
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

        val newTaskRequest = NewSubtaskRequest(
            assignedTo = assignedTo,
            creator = user?.id ?: "",
            description = viewState.description.value.toString(),
            doneCommentsRequired = viewState.doneCommentsRequired.value ?: false,
            doneImageRequired = viewState.doneImageRequired.value ?: false,
            dueDate = viewState.dueDate,
            isMultiTaskSubTask = false,
            state = state,
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

}