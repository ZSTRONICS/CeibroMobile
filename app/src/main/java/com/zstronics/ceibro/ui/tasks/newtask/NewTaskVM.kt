package com.zstronics.ceibro.ui.tasks.newtask

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.repos.chat.messages.MessagesResponse
import com.zstronics.ceibro.data.repos.chat.room.Member
import com.zstronics.ceibro.data.repos.projects.IProjectRepository
import com.zstronics.ceibro.data.repos.projects.projectsmain.ProjectsWithMembersResponse
import com.zstronics.ceibro.data.repos.task.ITaskRepository
import com.zstronics.ceibro.data.repos.task.models.NewTaskRequest
import com.zstronics.ceibro.data.repos.task.models.NewTaskRequestNoAdvanceOptions
import com.zstronics.ceibro.data.sessions.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NewTaskVM @Inject constructor(
    override val viewState: NewTaskState,
    private val projectRepository: IProjectRepository,
    private val taskRepository: ITaskRepository,
    private val sessionManager: SessionManager
) : HiltBaseViewModel<INewTask.State>(), INewTask.ViewModel {
    val user = sessionManager.getUser().value

    private var _allProjects: List<ProjectsWithMembersResponse.ProjectDetail> = listOf()
    private val _projectMembers: MutableLiveData<List<Member>> = MutableLiveData(arrayListOf())
    private val _projectNames: MutableLiveData<List<String>> = MutableLiveData(arrayListOf())
    private val _projectMemberNames: MutableLiveData<List<String>> = MutableLiveData(arrayListOf())
    private val _taskAdmins: MutableLiveData<ArrayList<Member>> = MutableLiveData(arrayListOf())
    private val _taskAssignee: MutableLiveData<ArrayList<Member>> = MutableLiveData(arrayListOf())

    val projectMembers: LiveData<List<Member>> = _projectMembers
    val projectNames: LiveData<List<String>> = _projectNames
    val projectMemberNames: LiveData<List<String>> = _projectMemberNames
    val taskAdmins: MutableLiveData<ArrayList<Member>> = _taskAdmins
    val taskAssignee: MutableLiveData<ArrayList<Member>> = _taskAssignee

    var projectId = ""

    init {
        loadProjects()
    }

    override fun loadProjects() {
        launch {
            loading(true)
            when (val response = projectRepository.getProjectsWithMembers()) {

                is ApiResponse.Success -> {
                    response.data.projectDetails.let { projects ->
                        if (projects.isNotEmpty()) {
                            _allProjects = projects
                            _projectNames.postValue(projects.map { it.title })
                            val firstProject = projects.first()
                            _projectMembers.postValue(firstProject.projectMembers)
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

    fun onProjectSelect(position: Int) {
        val selectedProject = _allProjects[position]
        projectId = selectedProject.id

        val member = Member(
            firstName = user?.firstName ?: "",
            surName = user?.surName ?: "",
            id = user?.id ?: "",
            profilePic = user?.profilePic ?: "",
            companyName = user?.companyName ?: ""
        )
        val projectMemb = selectedProject.projectMembers as MutableList
        if (projectMemb.contains(member) != true) {
            projectMemb.add(member)
        }
        _projectMembers.value = projectMemb
        _projectMemberNames.value = projectMemb.map { it.firstName + " " + it.surName }

        val admins = _taskAdmins.value
        if (admins?.contains(member) != true) {
            admins?.add(member)
        }
        _taskAdmins.value = admins
    }

    fun onAdminSelect(position: Int) {
        val member: Member? = projectMembers.value?.get(position)
        val admins = _taskAdmins.value
        if (admins?.contains(member) == true) {
            if ((member?.id ?: "") == user?.id){
                alert("Creator cannot be removed")
            }
            else {
                admins.remove(member)
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
        if (assignees?.contains(member) == true) {
            assignees.remove(member)
        } else {
            if (member != null) {
                assignees?.add(member)
            }
        }
        _taskAssignee.value = assignees
    }

    fun removeAdmin(data: Member) {
        val admins = _taskAdmins.value

        if (data.id == user?.id){
            alert("Creator cannot be removed")
        }
        else {
            admins?.remove(data)
        }
        _taskAdmins.value = admins
    }

    fun removeAssignee(data: Member) {
        val assignee = _taskAssignee.value
        assignee?.remove(data)
        _taskAssignee.value = assignee
    }

    fun createNewTask(state: String) {
//        val member: Member = Member(
//            firstName = user?.firstName ?: "",
//            surName = user?.surName ?: "",
//            id = user?.id ?: "",
//            profilePic = user?.profilePic ?: "",
//            companyName = user?.companyName ?: ""
//        )
//        val admins1 = _taskAdmins.value
//        admins1?.add(member)
//        _taskAdmins.value = admins1


        val admins = taskAdmins.value?.map { it.id } as MutableList
        admins.add(user?.id ?: "")          //user who is creating the task, MUST be a part of admins also
        val assignedTo = taskAssignee.value?.map { it.id } ?: listOf()
        val newTaskRequest = NewTaskRequestNoAdvanceOptions(
            admins = admins,
            assignedTo = assignedTo,
            creator = sessionManager.getUser().value?.id ?: "",
            dueDate = viewState.dueDate,
            isMultiTask = viewState.isMultiTask.value ?: false,
            project = projectId,
            state = state,
            title = viewState.taskTitle.value ?: ""
        )

        launch {
            loading(true)
            taskRepository.newTaskNoAdvanceOptions(newTaskRequest) { isSuccess, error ->
                loading(false, error)
                if (isSuccess)
                    handlePressOnView(1)
            }
        }
    }
}