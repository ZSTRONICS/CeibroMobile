package com.zstronics.ceibro.ui.tasks.editsubtaskdetails

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.database.models.subtask.AllSubtask
import com.zstronics.ceibro.data.database.models.tasks.CeibroTask
import com.zstronics.ceibro.data.repos.chat.room.Member
import com.zstronics.ceibro.data.repos.projects.IProjectRepository
import com.zstronics.ceibro.data.repos.task.TaskRepository
import com.zstronics.ceibro.data.repos.task.models.SubtaskStatusData
import com.zstronics.ceibro.data.sessions.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class EditSubTaskDetailsVM @Inject constructor(
    override val viewState: EditSubTaskDetailsState,
    val sessionManager: SessionManager,
    private val projectRepository: IProjectRepository,
    val taskRepository: TaskRepository
) : HiltBaseViewModel<IEditSubTaskDetails.State>(), IEditSubTaskDetails.ViewModel {
    val user = sessionManager.getUser().value

    private val _subtask: MutableLiveData<AllSubtask> = MutableLiveData()
    val subtask: LiveData<AllSubtask> = _subtask

    private val _subTaskStatus: MutableLiveData<List<SubtaskStatusData>> = MutableLiveData()
    val subTaskStatus: LiveData<List<SubtaskStatusData>> = _subTaskStatus

    private val _projectMembers: MutableLiveData<List<Member>> = MutableLiveData(arrayListOf())
    val projectMembers: LiveData<List<Member>> = _projectMembers

    private val _projectMemberNames: MutableLiveData<List<String>> = MutableLiveData(arrayListOf())
    val projectMemberNames: LiveData<List<String>> = _projectMemberNames

    private val _subtaskAssignee: MutableLiveData<ArrayList<Member>> = MutableLiveData(arrayListOf())
    val subtaskAssignee: MutableLiveData<ArrayList<Member>> = _subtaskAssignee

    override fun getSubTaskStatuses(subTaskId: String) {
        launch {
            loading(true)
            taskRepository.getSubtaskStatuses(subTaskId) { isSuccess, message, subtaskStatusData ->
                if (isSuccess) {
                    loading(false, "")
                    _subTaskStatus.postValue(subtaskStatusData)
                }
                else {
                    loading(false, message)
                }
            }
        }
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


    override fun onFirsTimeUiCreate(bundle: Bundle?) {
        super.onFirsTimeUiCreate(bundle)

        val subtaskParcel: AllSubtask? = bundle?.getParcelable("subtask")

        _subtask.value = subtaskParcel
        subtaskParcel?.id?.let { getSubTaskStatuses(it) }
        subtaskParcel?.taskData?.project?.id?.let { loadMemberByProjectId(it) }

    }


    fun onAssigneeSelect(position: Int) {
        val member: Member? = projectMembers.value?.get(position)
        val assignees = _subtaskAssignee.value

        val selectedMember = assignees?.find { it.id == member?.id }

        if (selectedMember != null) {
            assignees.remove(selectedMember)
        } else {
            if (member != null) {
                assignees?.add(member)
            }
        }
        _subtaskAssignee.value = assignees
    }

    fun removeAssignee(data: Member) {
        val assignee = _subtaskAssignee.value
        assignee?.remove(data)
        _subtaskAssignee.value = assignee
    }

}