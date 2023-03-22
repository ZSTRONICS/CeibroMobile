package com.zstronics.ceibro.ui.projects.newproject.group

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.repos.projects.IProjectRepository
import com.zstronics.ceibro.data.repos.projects.group.CreateGroupRequest
import com.zstronics.ceibro.data.repos.projects.group.ProjectGroup
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.ui.socket.LocalEvents
import dagger.hilt.android.lifecycle.HiltViewModel
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject

@HiltViewModel
class ProjectGroupVM @Inject constructor(
    override val viewState: ProjectGroupState,
    val sessionManager: SessionManager,
    private val projectRepository: IProjectRepository
) : HiltBaseViewModel<IProjectGroup.State>(), IProjectGroup.ViewModel {

    private val _groups: MutableLiveData<ArrayList<ProjectGroup>?> =
        MutableLiveData(arrayListOf())
    val groups: LiveData<ArrayList<ProjectGroup>?> = _groups

    init {
        EventBus.getDefault().register(this)
    }

    fun getGroups(id: String?) {
        launch {
            when (val response = projectRepository.getGroups(id ?: "")) {
                is ApiResponse.Success -> {
                    val newGroups = response.data.result as ArrayList<ProjectGroup>?

                    val adminGroup = newGroups?.find { it.isDefaultGroup }
                    val adminIndex = newGroups?.indexOf(adminGroup)
                    val newGroupList: ArrayList<ProjectGroup> = arrayListOf()
                    adminGroup?.let { newGroupList.add(it) }                  //keeping the admin group on top of the list

                    if (adminIndex != null && adminIndex > -1) {
                        newGroups.removeAt(adminIndex)
                    }
                    val sortedList = newGroups?.sortedBy { it.createdAt }
                    val reversedList = sortedList?.asReversed()          //sorting that newly created comes on top

                    if (reversedList != null) {
                        newGroupList.addAll(reversedList)
                    }

                    _groups.postValue(newGroupList)

                }
                is ApiResponse.Error -> {
                    alert(response.error.message)
                }
            }
        }
    }

    fun addGroup(id: String?, group: String) {
        val oldList = groups.value
        val groupExist = oldList?.find { it.name == group }
        if (groupExist != null) {
            alert("Duplicate group")
            return
        }
//        oldList?.add(ProjectGroup(name = group))
//        oldList?.let {
//            _groups.value = it
//        }
        addGroupAPI(id, group)
    }

    private fun addGroupAPI(id: String?, group: String) {
        launch {
            when (val response =
                projectRepository.createGroup(id ?: "", CreateGroupRequest(group))) {
                is ApiResponse.Success -> {
                    getGroups(id)
                }
                is ApiResponse.Error -> {
                    alert(response.error.message)
                }
            }
        }
    }

    fun updateGroup(projectId: String, id: String?, group: String) {
        val old = groups.value
        val groupFound = old?.find { it.id == id }
        if (groupFound != null) {
            val index = old.indexOf(groupFound)
            if (index != -1) {
                old.removeAt(index)
                groupFound.name = group
                old.add(index, groupFound)
            }
            old.let {
                _groups.value = it
            }
        }
        updateGroupAPI(projectId, id, group)
    }

    private fun updateGroupAPI(projectId: String, id: String?, group: String) {

        launch {
            when (val response =
                projectRepository.updateGroup(id ?: "", CreateGroupRequest(group))) {
                is ApiResponse.Success -> {
                    getGroups(projectId)
                }
                is ApiResponse.Error -> {
                    alert(response.error.message)
                }
            }
        }
    }

    fun deleteGroup(data: ProjectGroup, position: Int, id: String) {
        if (!data.isDefaultGroup) {
            deleteGroupAPI(position, data.project, id)
        } else {
            alert("Cannot remove default group")
        }
    }

    private fun deleteGroupAPI(position: Int, projectId: String, id: String) {
        launch {
            loading(true)
            when (val response =
                projectRepository.deleteGroup(id)) {
                is ApiResponse.Success -> {
                    val old = groups.value
                    old?.removeAt(position)
                    old?.let {
                        _groups.value = it
                    }
                    loading(false)
                    getGroups(projectId)
                }
                is ApiResponse.Error -> {
                    loading(false, response.error.message)
                    getGroups(projectId)
                }
            }
        }
    }




    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onGroupCreatedEvent(event: LocalEvents.GroupCreatedEvent?) {
        val oldGroups = groups.value
        val selectedGroup = oldGroups?.find { it.id == event?.newGroup?.id }

        if (selectedGroup == null) {
            //it means new group added
            event?.newGroup?.let { oldGroups?.add(it) }
        }
        else {
            //it means an update of a group is received
            val index = oldGroups.indexOf(selectedGroup)
            if (index > -1) {
                event?.newGroup?.let { oldGroups.set(index, it) }
            }
        }

        val adminGroup = oldGroups?.find { it.isDefaultGroup }
        val adminIndex = oldGroups?.indexOf(adminGroup)
        val newGroupList: ArrayList<ProjectGroup> = arrayListOf()
        adminGroup?.let { newGroupList.add(it) }                  //keeping the admin group on top of the list

        if (adminIndex != null && adminIndex > -1) {
            oldGroups.removeAt(adminIndex)
        }
        val sortedList = oldGroups?.sortedBy { it.createdAt }
        val reversedList = sortedList?.asReversed()          //sorting that newly created comes on top

        if (reversedList != null) {
            newGroupList.addAll(reversedList)
        }

        _groups.postValue(newGroupList)
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onGroupRefreshEvent(event: LocalEvents.GroupRefreshEvent?) {
        getGroups(event?.projectId ?: "")
    }

    override fun onCleared() {
        super.onCleared()
        EventBus.getDefault().unregister(this)
    }
}