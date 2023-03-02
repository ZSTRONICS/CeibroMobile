package com.zstronics.ceibro.ui.tasks.newtask

import android.content.Context
import android.os.Bundle
import android.os.Handler
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.database.models.tasks.CeibroTask
import com.zstronics.ceibro.data.local.FileAttachmentsDataSource
import com.zstronics.ceibro.data.repos.chat.room.Member
import com.zstronics.ceibro.data.repos.dashboard.IDashboardRepository
import com.zstronics.ceibro.data.repos.dashboard.attachment.AttachmentModules
import com.zstronics.ceibro.data.repos.projects.IProjectRepository
import com.zstronics.ceibro.data.repos.projects.projectsmain.ProjectsWithMembersResponse
import com.zstronics.ceibro.data.repos.task.ITaskRepository
import com.zstronics.ceibro.data.repos.task.models.NewTaskRequestNoAdvanceOptions
import com.zstronics.ceibro.data.repos.task.models.UpdateDraftTaskRequestNoAdvanceOptions
import com.zstronics.ceibro.data.repos.task.models.UpdateTaskRequestNoAdvanceOptions
import com.zstronics.ceibro.data.sessions.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NewTaskVM @Inject constructor(
    override val viewState: NewTaskState,
    private val projectRepository: IProjectRepository,
    private val taskRepository: ITaskRepository,
    private val sessionManager: SessionManager,
    private val dashboardRepository: IDashboardRepository,
    private val fileAttachmentsDataSource: FileAttachmentsDataSource
) : HiltBaseViewModel<INewTask.State>(), INewTask.ViewModel {
    val user = sessionManager.getUser().value
    var isNewTask = true
    var projectIndex = 0

    private val _task: MutableLiveData<CeibroTask> = MutableLiveData()
    val task: LiveData<CeibroTask> = _task

    private var _allProjects: List<ProjectsWithMembersResponse.ProjectDetail> = listOf()
    private val _projectMembers: MutableLiveData<List<Member>> = MutableLiveData(arrayListOf())
    private val _projectNames: MutableLiveData<List<String>> = MutableLiveData(arrayListOf())
    private val _projectMemberNames: MutableLiveData<List<String>> = MutableLiveData(arrayListOf())
    private val _taskAdmins: MutableLiveData<ArrayList<Member>?> = MutableLiveData(arrayListOf())
    private val _taskAssignee: MutableLiveData<ArrayList<Member>?> = MutableLiveData(arrayListOf())

    val projectMembers: LiveData<List<Member>> = _projectMembers
    val projectNames: LiveData<List<String>> = _projectNames
    val projectMemberNames: LiveData<List<String>> = _projectMemberNames
    val taskAdmins: MutableLiveData<ArrayList<Member>?> = _taskAdmins
    val taskAssignee: MutableLiveData<ArrayList<Member>?> = _taskAssignee

    var projectId = ""
    var taskId = ""


    override fun onFirsTimeUiCreate(bundle: Bundle?) {
        super.onFirsTimeUiCreate(bundle)
        val isNTask: Boolean = bundle?.getBoolean("newTask") ?: true
        isNewTask = isNTask
        if (isNTask) {
            //Do nothing just keep going with flow to create new task
            loadProjects()
        } else {
            val taskParcel: CeibroTask? = bundle?.getParcelable("task")
            taskParcel?.let {
                loadProjects(taskParcel)
            }

        }
    }

    override fun loadProjects() {
        launch {
            loading(true)
            when (val response = projectRepository.getProjectsWithMembers(true)) {

                is ApiResponse.Success -> {
                    response.data.projectDetails.let { projects ->
                        if (projects.isNotEmpty()) {
                            _allProjects = projects
                            _projectNames.postValue(projects.map { it.title })

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

    override fun loadProjects(taskParcel: CeibroTask) {
        launch {
            loading(true)
            when (val response = projectRepository.getProjectsWithMembers(true)) {

                is ApiResponse.Success -> {
                    response.data.projectDetails.let { projects ->
                        if (projects.isNotEmpty()) {
                            _allProjects = projects
                            _projectNames.postValue(projects.map { it.title })

                        }
                    }
                    loading(false)
                    val handler = Handler()
                    handler.postDelayed(Runnable {
                        setEditTaskDetails(taskParcel)
                    }, 50)

                }
                is ApiResponse.Error -> {
                    loading(false)
                }
            }
        }
    }

    private fun setEditTaskDetails(taskParcel: CeibroTask) {

        taskParcel.let {
            val projectName = projectNames.value
            var index = 0
            var selectedProjectName = ""

            if (projectName != null) {
                for (projectN in projectName) {
                    if (taskParcel.project.title == projectN) {
                        selectedProjectName = taskParcel.project.title
                        break
                    }
                    index++
                }
            }
            projectIndex = index
            projectId = taskParcel.project.id
            taskId = taskParcel._id
            _task.value = taskParcel

            val assigneeList = taskParcel.assignedTo.map {
                Member(
                    companyName = "",
                    firstName = it.firstName,
                    surName = it.surName,
                    id = it.id,
                    profilePic = it.profilePic
                )
            }
            _taskAssignee.value = assigneeList as ArrayList<Member>

            val adminList = taskParcel.admins.map {
                Member(
                    companyName = "",
                    firstName = it.firstName,
                    surName = it.surName,
                    id = it.id,
                    profilePic = it.profilePic
                )
            }
            _taskAdmins.value = adminList as ArrayList<Member>

        }
    }

    fun onProjectSelect(position: Int) {
        val selectedProject = _allProjects[position]
        if (selectedProject.id != projectId) {
            val tempAdmins = _taskAdmins.value
            tempAdmins?.removeAll(tempAdmins)
            _taskAdmins.value = tempAdmins

            val tempAssignee = _taskAssignee.value
            tempAssignee?.removeAll(tempAssignee)
            _taskAssignee.value = tempAssignee
        }
        projectId = selectedProject.id

        val member = Member(
            firstName = user?.firstName ?: "",
            surName = user?.surName ?: "",
            id = user?.id ?: "",
            profilePic = user?.profilePic ?: "",
            companyName = user?.companyName ?: ""
        )
        val projectMemb = selectedProject.projectMembers as MutableList
        val currentUser = projectMemb.find { it.id == member.id }
        if (currentUser == null) {
            projectMemb.add(member)
        }
        _projectMembers.value = projectMemb
        _projectMemberNames.value = projectMemb.map { it.firstName + " " + it.surName }


        val admins = _taskAdmins.value
        val selectedMember = admins?.find { it.id == member.id }
        if (selectedMember == null) {
            admins?.add(member)
        }
        _taskAdmins.value = admins
    }

    fun onAdminSelect(position: Int) {
        val member: Member? = projectMembers.value?.get(position)
        val admins = _taskAdmins.value

        val selectedMember = admins?.find { it.id == member?.id }

        if (selectedMember != null) {
            if ((member?.id ?: "") == user?.id) {
                alert("Creator cannot be removed")
            } else {
                admins.remove(selectedMember)
            }
        } else {
            if (member != null) {
                admins?.add(member)
            }
        }
        _taskAdmins.value = admins
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

    fun removeAdmin(data: Member) {
        val admins = _taskAdmins.value

        if (data.id == user?.id) {
            alert("Creator cannot be removed")
        } else {
            admins?.remove(data)
        }
        _taskAdmins.value = admins
    }

    fun removeAssignee(data: Member) {
        val assignee = _taskAssignee.value
        assignee?.remove(data)
        _taskAssignee.value = assignee
    }

    fun createNewTask(
        state: String,
        context: Context,
        success: (taskId: String) -> Unit,
        back: () -> Unit
    ) {

        if (viewState.taskTitle.value.toString() == "") {
            alert("Please enter task title")
        } else if (projectId == "") {
            alert("Please select a project")
        } else if (viewState.dueDate == "") {
            alert("Please select a due date")
        } else {
            val admins = taskAdmins.value?.map { it.id } as MutableList
            val assignedTo = taskAssignee.value?.map { it.id } ?: listOf()
            val newTaskRequest = NewTaskRequestNoAdvanceOptions(
                admins = admins,
                assignedTo = assignedTo,
                creator = sessionManager.getUser().value?.id ?: "",
                dueDate = viewState.dueDate,
                isMultiTask = viewState.isMultiTask.value ?: false,
                project = projectId,
                state = state,
                description = viewState.description.value.toString(),
                title = viewState.taskTitle.value.toString()
            )

            launch {
                loading(true)
                taskRepository.newTaskNoAdvanceOptions(newTaskRequest) { isSuccess, error, data ->
                    if (isSuccess) {
                        if (fileUriList.value?.isNotEmpty() == true) {
                            data?._id?.let {
                                success(it)
                                uploadFiles(AttachmentModules.Task.name, it, context)
                            }
                        }
                        loading(false, "")
                        back.invoke()
                    } else {
                        loading(false, error)
                    }
                }
            }
        }
    }


    fun updateTask(taskId: String, state: String, back: () -> Unit) {

        val admins = taskAdmins.value?.map { it.id } as MutableList
        val assignedTo = taskAssignee.value?.map { it.id } ?: listOf()
        val updateTaskRequest = UpdateDraftTaskRequestNoAdvanceOptions(
            admins = admins,
            assignedTo = assignedTo,
            dueDate = viewState.dueDate,
            isMultiTask = viewState.isMultiTask.value ?: false,
            project = projectId,
            state = state,
            description = viewState.description.value.toString(),
            title = viewState.taskTitle.value.toString() ?: ""
        )

        launch {
            loading(true)
            taskRepository.updateTaskByIdNoAdvanceOptions(
                taskId,
                updateTaskRequest
            ) { isSuccess, error ->
                if (isSuccess) {
                    loading(false, "Task Updated Successfully")
                    back.invoke()
                } else {
                    loading(false, error)
                }
            }
        }
    }

    fun updateTaskWithNoState(taskId: String, back: () -> Unit) {

        val admins = taskAdmins.value?.map { it.id } as MutableList
        val assignedTo = taskAssignee.value?.map { it.id } ?: listOf()
        val updateTaskRequest = UpdateTaskRequestNoAdvanceOptions(
            admins = admins,
            assignedTo = assignedTo,
            description = viewState.description.value.toString()
        )

        launch {
            loading(true)
            taskRepository.updateTaskNoStateNoAdvanceOptions(
                taskId,
                updateTaskRequest
            ) { isSuccess, error ->
                if (isSuccess) {
                    loading(false, "Task Updated Successfully")
                    back.invoke()
                } else {
                    loading(false, error)
                }
            }
        }
    }
}