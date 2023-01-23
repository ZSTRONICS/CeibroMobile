package com.zstronics.ceibro.ui.chat.newchat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.repos.chat.IChatRepository
import com.zstronics.ceibro.data.repos.chat.messages.NewGroupChatRequest
import com.zstronics.ceibro.data.repos.chat.room.Member
import com.zstronics.ceibro.data.repos.projects.IProjectRepository
import com.zstronics.ceibro.data.repos.projects.projectsmain.ProjectsWithMembersResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NewChatVM @Inject constructor(
    override val viewState: NewChatState,
    private val projectRepository: IProjectRepository,
    private val chatRepository: IChatRepository
) : HiltBaseViewModel<INewChat.State>(), INewChat.ViewModel {

    private var _allProjects: List<ProjectsWithMembersResponse.ProjectDetail> = listOf()
    private val _projectNames: MutableLiveData<List<String>> = MutableLiveData(arrayListOf())

    private val _projectMembers: MutableLiveData<List<Member>> = MutableLiveData(arrayListOf())
    val projectMembers: LiveData<List<Member>> = _projectMembers

    private val _projectGroups: MutableLiveData<List<ProjectsWithMembersResponse.ProjectDetail.Group>> =
        MutableLiveData(arrayListOf())
    val projectGroups: LiveData<List<ProjectsWithMembersResponse.ProjectDetail.Group>> =
        _projectGroups

    val projectNames: LiveData<List<String>> = _projectNames

    var projectId = ""

    fun createGroupChat(request: NewGroupChatRequest) {
        launch {
            loading(true)
            when (val response = chatRepository.createGroupChat(request)) {

                is ApiResponse.Success -> {
                    loading(false)
                    handlePressOnView(111)
                }

                is ApiResponse.Error -> {
                    loading(false)
                }
            }
        }
    }

    init {
        loadProjects()
    }

    override fun loadProjects() {
        launch {
            loading(true)
            when (val response = projectRepository.getProjectsWithMembers(false)) {

                is ApiResponse.Success -> {
                    response.data.projectDetails.let { projects ->
                        if (projects.isNotEmpty()) {
                            _allProjects = projects
                            _projectNames.postValue(projects.map { it.title })
                            val firstProject = projects.first()
                            _projectMembers.postValue(firstProject.projectMembers)
                            _projectGroups.postValue(firstProject.groups)
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
        _projectMembers.value = selectedProject.projectMembers
        _projectGroups.value = selectedProject.groups
    }
}