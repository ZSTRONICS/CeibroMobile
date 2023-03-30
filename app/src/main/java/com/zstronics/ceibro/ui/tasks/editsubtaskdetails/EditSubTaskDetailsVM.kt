package com.zstronics.ceibro.ui.tasks.editsubtaskdetails

import android.os.Bundle
import android.view.View
import android.widget.AutoCompleteTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.database.models.subtask.AllSubtask
import com.zstronics.ceibro.data.database.models.subtask.AssignedTo
import com.zstronics.ceibro.data.database.models.tasks.TaskMember
import com.zstronics.ceibro.data.repos.chat.room.Member
import com.zstronics.ceibro.data.repos.projects.IProjectRepository
import com.zstronics.ceibro.data.repos.task.TaskRepository
import com.zstronics.ceibro.data.repos.task.models.*
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.ui.tasks.subtask.SubTaskStatus
import com.zstronics.ceibro.ui.tasks.task.TaskStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import koleton.api.hideSkeleton
import koleton.api.loadSkeleton
import javax.inject.Inject

@HiltViewModel
class EditSubTaskDetailsVM @Inject constructor(
    override val viewState: EditSubTaskDetailsState,
    val sessionManager: SessionManager,
    private val projectRepository: IProjectRepository,
    val taskRepository: TaskRepository
) : HiltBaseViewModel<IEditSubTaskDetails.State>(), IEditSubTaskDetails.ViewModel {
    val user = sessionManager.getUser().value

    private val _subtask: MutableLiveData<AllSubtask?> = MutableLiveData()
    val subtask: LiveData<AllSubtask?> = _subtask

    private val _subTaskStatus: MutableLiveData<List<SubtaskStatusData>> = MutableLiveData()
    val subTaskStatus: LiveData<List<SubtaskStatusData>> = _subTaskStatus

    private val _assignToMembers: MutableLiveData<List<AssignedTo>?> = MutableLiveData()
    val assignToMembers: LiveData<List<AssignedTo>?> = _assignToMembers

    private val _projectMembers: MutableLiveData<List<Member>> = MutableLiveData(arrayListOf())
    val projectMembers: LiveData<List<Member>> = _projectMembers

    private val _projectMemberNames: MutableLiveData<List<String>> = MutableLiveData(arrayListOf())
    val projectMemberNames: LiveData<List<String>> = _projectMemberNames

    private val _subtaskAssignee: MutableLiveData<ArrayList<Member>?> = MutableLiveData(arrayListOf())
    val subtaskAssignee: MutableLiveData<ArrayList<Member>?> = _subtaskAssignee

    override fun getSubTaskStatuses(subTaskId: String) {
//        launch {
//            loading(true)
//            taskRepository.getSubtaskStatuses(subTaskId) { isSuccess, message, subtaskStatusData ->
//                if (isSuccess) {
//                    loading(false, "")
//                    _subTaskStatus.postValue(subtaskStatusData)
//                }
//                else {
//                    loading(false, message)
//                }
//            }
//        }
    }

    fun loadMemberByProjectId(
        projectId: String,
        skeletonLayout: ConstraintLayout,
        editDetailsMemberSpinner: AutoCompleteTextView
    ) {
        launch {
            editDetailsMemberSpinner.visibility = View.GONE
            skeletonLayout.visibility = View.VISIBLE
            skeletonLayout.loadSkeleton()

            val subT = subtask.value
            val subTaskMemList: ArrayList<TaskMember> = ArrayList()
            if (subT != null) {
                for (assign in subT.assignedTo) {
                    for (member in assign.members) {
                        subTaskMemList.add(member)
                    }
                }
            }

            when (val response = projectRepository.getProjectMembers(projectId)) {
                is ApiResponse.Success -> {
                    val onlyMembers: List<Member> = response.data.members.filter { it.user != null }.map {
                        it.user!!
                    }
                    val responseMembers = onlyMembers as ArrayList<Member>
                    val checkMembers = onlyMembers as ArrayList<Member>

                    // Sorting those members which are already part of subtask, those will be removed from add dropdown
                    for (stMember in subTaskMemList) {
                        val selectMem = checkMembers.find { it.id == stMember.id }
                        if (selectMem != null) {
                            responseMembers.remove(selectMem)
                        }
                    }

                    responseMembers.let { members ->
                        _projectMembers.postValue(members)
                        _projectMemberNames.postValue(members.map { it.firstName + " " + it.surName })
                    }
                    editDetailsMemberSpinner.visibility = View.VISIBLE
                    skeletonLayout.visibility = View.GONE
                    skeletonLayout.hideSkeleton()
                }

                is ApiResponse.Error -> {
                    editDetailsMemberSpinner.visibility = View.VISIBLE
                    skeletonLayout.visibility = View.GONE
                    skeletonLayout.hideSkeleton()
                }
            }
        }
    }


    override fun onFirsTimeUiCreate(bundle: Bundle?) {
        super.onFirsTimeUiCreate(bundle)

        val subtaskParcel: AllSubtask? = bundle?.getParcelable("subtask")

        _subtask.value = subtaskParcel

        val assignTo = subtaskParcel?.assignedTo
        _assignToMembers.postValue(assignTo)

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





    fun addMemberToSubtask(subTask: AllSubtask?, state: String) {
        val newAssigneeMembersId: ArrayList<String> = subtaskAssignee.value?.map { it.id } as ArrayList<String>
        val newAssignedTo: AddMemberSubtaskRequest.AssignedTo =
            AddMemberSubtaskRequest.AssignedTo(
                addedBy = user?.id,
                members = newAssigneeMembersId
            )


        var newMembersAdded = false
        val finalAssignedTo = arrayListOf<AddMemberSubtaskRequest.AssignedTo>()
        val subTaskMemIdList: ArrayList<String> = ArrayList()

        if (subTask != null) {
            for (assign in subTask.assignedTo) {

                if (assign.addedBy.id == user?.id) {
                    for (member in assign.members) {
                        subTaskMemIdList.add(member.id)
                    }
                    newMembersAdded = true
                    subTaskMemIdList.addAll(newAssigneeMembersId)
                    val oldWithNewAssignedTo: AddMemberSubtaskRequest.AssignedTo =
                        AddMemberSubtaskRequest.AssignedTo(
                            addedBy = assign.addedBy.id,
                            members = subTaskMemIdList
                        )

                    finalAssignedTo.add(oldWithNewAssignedTo)
                }
                else {
                    val oldAssignedTo: AddMemberSubtaskRequest.AssignedTo =
                        AddMemberSubtaskRequest.AssignedTo(
                            addedBy = assign.addedBy.id,
                            members = assign.members.map { member ->
                                member.id
                            }
                        )

                    finalAssignedTo.add(oldAssignedTo)
                }
            }
        }
        if (!newMembersAdded) {
            finalAssignedTo.add(newAssignedTo)
        }

        println("AssignTO: $finalAssignedTo")

        var highestState = state.lowercase()
        val newAssigneeStates =
            newAssigneeMembersId.map { id ->
                AddMemberSubtaskRequest.State(
                    userId = id,
                    userState = if (isSubTaskCreator(id, subTask?.creator)) {
                        highestState = SubTaskStatus.ACCEPTED.name.lowercase()
                        highestState
                    } else state.lowercase()
                )
            }

        val oldAssigneeStates = arrayListOf<AddMemberSubtaskRequest.State>()

        if (subTask != null) {
            for (userState in subTask.state!!) {
                val foundUserStateID = newAssigneeMembersId.find { it == userState.userId }
                if (foundUserStateID.equals(userState.userId)) {
                //Skipping this user because he was not old assignee but was a task-admin, so now he is going to be in assignTo member, so his old state will be skipped
                }
                else {
                    val oldUserState: List<AddMemberSubtaskRequest.State> = listOf(
                        AddMemberSubtaskRequest.State(
                            userId = userState.userId,
                            userState = userState.userState
                        )
                    )
                    oldAssigneeStates.addAll(oldUserState)
                }
            }
        }

        val finalStates = arrayListOf<AddMemberSubtaskRequest.State>()

        if (oldAssigneeStates.isNotEmpty()) {
            finalStates.addAll(oldAssigneeStates)
        }
        if (newAssigneeStates.isNotEmpty()) {
            finalStates.addAll(newAssigneeStates)
        }


        val addMemberSubtaskRequest = AddMemberSubtaskRequest(
            assignedTo = finalAssignedTo,
            state = finalStates,
        )

        launch {
            loading(true)
            taskRepository.updateMemberInSubTask(subTask?.id?: "", addMemberSubtaskRequest) { isSuccess, error, changedSubTask ->
                if (isSuccess) {
                    loading(false, "SubTask Updated Successfully")
                    if (changedSubTask != null) {
                        val assignTo = changedSubTask.assignedTo
                        _subtask.postValue(changedSubTask)
                        _assignToMembers.postValue(assignTo)
                    }
                    val assignee = _subtaskAssignee.value
                    assignee?.removeAll(assignee)
                    _subtaskAssignee.value = assignee
                } else {
                    loading(false, error)
                }
            }
        }
    }


    fun removeMemberFromSubtask(taskId: String, subTaskId: String, memberId: String) {
        launch {
            loading(true)
            taskRepository.removeSubTaskMember(taskId, subTaskId, memberId) { isSuccess, error, changedSubTask ->
                if (isSuccess) {
                    loading(false, "")
                    if (changedSubTask != null) {
                        val assignTo = changedSubTask.assignedTo
                        _subtask.postValue(changedSubTask)
                        _assignToMembers.postValue(assignTo)
                    }
                }
                else {
                    loading(false, error)
                }
            }
        }
    }

    fun markAsDoneForSubtaskMember(taskId: String, subTaskId: String, memberId: String) {
        launch {
            loading(true)
            taskRepository.markAsDoneForSubtaskMember(taskId, subTaskId, memberId) { isSuccess, error, changedSubTask ->
                if (isSuccess) {
                    loading(false, "")
                    if (changedSubTask != null) {
                        val assignTo = changedSubTask.assignedTo
                        _subtask.postValue(changedSubTask)
                        _assignToMembers.postValue(assignTo)
                    }
                }
                else {
                    loading(false, error)
                }
            }
        }
    }



    fun isTaskAdmin(userId: String?, admins: List<String>?): Boolean {
        var isAdmin = false
        val id: String? = admins?.find { it == userId }
        if (id.equals(userId)) {
            isAdmin = true
        }

        return isAdmin
    }

    private fun isSubTaskCreator(userId: String?, creator: TaskMember?): Boolean {
        var isCreator = false
        if (creator?.id.equals(userId)) {
            isCreator = true
        }
        return isCreator
    }
}