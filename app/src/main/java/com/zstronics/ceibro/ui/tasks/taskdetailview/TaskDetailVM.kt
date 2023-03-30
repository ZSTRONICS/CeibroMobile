package com.zstronics.ceibro.ui.tasks.taskdetailview

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.database.models.subtask.AllSubtask
import com.zstronics.ceibro.data.database.models.subtask.SubTaskStateItem
import com.zstronics.ceibro.data.database.models.tasks.CeibroTask
import com.zstronics.ceibro.data.database.models.tasks.TaskMember
import com.zstronics.ceibro.data.local.FileAttachmentsDataSource
import com.zstronics.ceibro.data.repos.dashboard.IDashboardRepository
import com.zstronics.ceibro.data.repos.task.ITaskRepository
import com.zstronics.ceibro.data.repos.task.models.UpdateSubTaskStatusRequest
import com.zstronics.ceibro.data.repos.task.models.UpdateSubTaskStatusWithoutCommentRequest
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.ui.socket.LocalEvents
import com.zstronics.ceibro.ui.tasks.subtask.SubTaskStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TaskDetailVM @Inject constructor(
    override val viewState: TaskDetailState,
    val sessionManager: SessionManager,
    private val taskRepository: ITaskRepository,
    private val fileAttachmentsDataSource: FileAttachmentsDataSource,
    private val dashboardRepository: IDashboardRepository
) : HiltBaseViewModel<ITaskDetail.State>(), ITaskDetail.ViewModel {
    val user = sessionManager.getUser().value
    val projects = sessionManager.getProjects().value

    private val _subTasksForStatus: MutableLiveData<List<AllSubtask>> = MutableLiveData()
    val subTasksForStatus: LiveData<List<AllSubtask>> = _subTasksForStatus

    private val _task: MutableLiveData<CeibroTask?> = MutableLiveData()
    val task: LiveData<CeibroTask?> = _task

    private val _subTasks: MutableLiveData<List<AllSubtask>> = MutableLiveData()
    val subTasks: LiveData<List<AllSubtask>> = _subTasks
    var originalSubTasks: List<AllSubtask> = listOf()

    override fun onFirsTimeUiCreate(bundle: Bundle?) {
        super.onFirsTimeUiCreate(bundle)
        val taskParcel: CeibroTask? = bundle?.getParcelable("task")
        _task.value = taskParcel

        taskParcel?._id?.let { getSubTasks(it) }
        taskParcel?.let {
            launch {
                when (val response =
                    dashboardRepository.getFilesByModuleId(module = "Task", moduleId = it._id)) {
                    is ApiResponse.Success -> {
                        response.data.results?.let { it1 -> fileAttachmentsDataSource.insertAll(it1) }
                    }
                    else -> {}
                }
            }
        }
    }

    override fun getSubTasks(taskId: String) {
        launch {
           originalSubTasks = taskRepository.getSubTaskByTaskId(taskId)
            _subTasks.postValue(originalSubTasks)
            _subTasksForStatus.postValue(originalSubTasks)
        }
    }

    fun deleteSubTask(subtaskId: String) {
        launch {
            loading(true)
            taskRepository.deleteSubTask(subtaskId) { isSuccess, message ->
                if (isSuccess) {
                    loading(false, "Subtask Deleted Successfully")
                    task.value?._id?.let { getSubTasks(it) }
                } else {
                    loading(false, message)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        task.value?._id?.let { getSubTasks(it) }
    }

    fun isCurrentTaskId(taskId: String?): Boolean {
        return taskId == task.value?._id
    }

    fun rejectSubTask(
        data: AllSubtask,
        state: SubTaskStatus,
        callBack: (result: Triple<Boolean, Boolean, Boolean>) -> Unit,
        onTaskDeleted: () -> Unit
    ) {
        val request = UpdateSubTaskStatusRequest(
            comment = "Test comment",
            state = state.name.lowercase(),
            subTaskId = data.id,
            taskId = data.taskId
        )
        launch {
            val result = taskRepository.rejectSubtask(request)
            val (apiCallSuccess, taskDeleted, subTaskDeleted) = result
            if (taskDeleted) {
                callBack.invoke(result)
                onTaskDeleted()
            } else {
                callBack.invoke(result)
            }
        }
    }

    fun updateSubtaskStatus(
        data: AllSubtask,
        state: SubTaskStatus,
        callBack: (result: Triple<Boolean, Boolean, Boolean>) -> Unit,
        onTaskDeleted: () -> Unit
    ) {
        val request = UpdateSubTaskStatusWithoutCommentRequest(
            state = state.name.lowercase(),
            subTaskId = data.id,
            taskId = data.taskId
        )
        launch {
            val result = taskRepository.updateSubtaskStatus(request)
            val (apiCallSuccess, taskDeleted, subTaskDeleted) = result
            if (taskDeleted) {
                callBack.invoke(result)
                onTaskDeleted()
            } else {
                callBack.invoke(result)
            }
        }
    }

    fun onApplySearch(query: String?) {
        if (query?.isEmpty() == true) {
            _subTasks.postValue(originalSubTasks)
            _subTasksForStatus.postValue(originalSubTasks)
            return
        }
        if (query != null) {
            val filtered = originalSubTasks.filter { it.title.contains(query, true) }
            _subTasks.postValue(filtered)
        }
        else {
            alert("Unable to search")
        }
    }

    fun onApplyFilters(
        projectId: String,
        selectedStatus: String,
        selectedDueDate: String,
        newMembers: List<TaskMember>?
    ) {
        val filtered =
            originalSubTasks.filter {
                (it.taskData?.project?.id == projectId || projectId.isEmpty())
                        && haveMembers(it.assignedToMembersOnly, newMembers)
                        && (getState(it.state).equals(selectedStatus, true) || selectedStatus.equals("All", true))
                        && (it.dueDate == selectedDueDate || selectedDueDate.isEmpty())
            }

        _subTasks.postValue(filtered)
        _subTasksForStatus.postValue(filtered)
    }

    fun resetFilters() {
        _subTasks.postValue(originalSubTasks)
        _subTasksForStatus.postValue(originalSubTasks)
    }

    fun applyStatusFilter(selectedStatus: String) {
        val filtered =
            originalSubTasks.filter {
                (getState(it.state).equals(selectedStatus, true) || selectedStatus.equals("ALL", true))
            }
        _subTasks.postValue(filtered)

        if (subTasksForStatus.value?.size != originalSubTasks.size) {
            _subTasksForStatus.postValue(originalSubTasks)
        }
    }

    private fun haveMembers(
        list: List<TaskMember>?,
        assigneeToMembers: List<TaskMember>?
    ): Boolean {
        // Return true if assigneeToMembers is null or empty
        if (assigneeToMembers == null || assigneeToMembers.isEmpty()) {
            return true
        }

        // Check if any of the assigneeToMembers ids are found in the list
        return if (list != null) {
            assigneeToMembers.any { assignee ->
                list.any { it.id == assignee.id }
            }
        } else {
            true
        }
    }

    fun getState(state: List<SubTaskStateItem>?): String {
        val foundState = state?.find { it.userId == user?.id }?.userState
        return foundState.toString()
    }
}