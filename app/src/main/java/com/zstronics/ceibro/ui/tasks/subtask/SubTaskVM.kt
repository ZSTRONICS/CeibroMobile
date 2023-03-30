package com.zstronics.ceibro.ui.tasks.subtask

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.database.models.subtask.AllSubtask
import com.zstronics.ceibro.data.database.models.subtask.SubTaskStateItem
import com.zstronics.ceibro.data.database.models.tasks.TaskMember
import com.zstronics.ceibro.data.repos.task.TaskRepository
import com.zstronics.ceibro.data.repos.task.models.UpdateSubTaskStatusRequest
import com.zstronics.ceibro.data.repos.task.models.UpdateSubTaskStatusWithoutCommentRequest
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.ui.socket.LocalEvents
import dagger.hilt.android.lifecycle.HiltViewModel
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject

@HiltViewModel
class SubTaskVM @Inject constructor(
    override val viewState: SubTaskState,
    val sessionManager: SessionManager,
    val taskRepository: TaskRepository
) : HiltBaseViewModel<ISubTask.State>(), ISubTask.ViewModel {
    val user = sessionManager.getUser().value

    private val _subTasksForStatus: MutableLiveData<List<AllSubtask>> = MutableLiveData()
    val subTasksForStatus: LiveData<List<AllSubtask>> = _subTasksForStatus

    private val _subTasks: MutableLiveData<List<AllSubtask>> = MutableLiveData()
    val subTasks: LiveData<List<AllSubtask>> = _subTasks
    var originalSubTasks: List<AllSubtask> = listOf()

    init {
        getSubTasks()
        EventBus.getDefault().register(this)
    }

    override fun getSubTasks() {
        launch {
            originalSubTasks = taskRepository.getAllSubtasks()
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
                    getSubTasks()
                } else {
                    loading(false, message)
                }
            }
        }
    }

    fun rejectSubTask(
        data: AllSubtask,
        state: SubTaskStatus,
        callBack: (result: Triple<Boolean, Boolean, Boolean>) -> Unit
    ) {
        val request = UpdateSubTaskStatusRequest(
            comment = "Test comment",
            state = state.name.lowercase(),
            subTaskId = data.id,
            taskId = data.taskId
        )
        loading(true)
        launch {
            val result = taskRepository.rejectSubtask(request)
            callBack.invoke(result)
            loading(false)
        }
    }

    fun updateSubtaskStatus(
        data: AllSubtask,
        state: SubTaskStatus,
        callBack: (result: Triple<Boolean, Boolean, Boolean>) -> Unit
    ) {
        val request = UpdateSubTaskStatusWithoutCommentRequest(
            state = state.name.lowercase(),
            subTaskId = data.id,
            taskId = data.taskId
        )
        loading(true)
        launch {
            val result = taskRepository.updateSubtaskStatus(request)
            callBack.invoke(result)
            loading(false)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onApplyFilters(filter: LocalEvents.ApplyFilterOnSubTask) {
        val filtered =
            originalSubTasks.filter {
                (it.taskData?.project?.id == filter.projectId || filter.projectId.isEmpty())
                        && haveMembers(it.assignedToMembersOnly, filter.assigneeToMembers)
                        && (getState(it.state).equals(
                    filter.selectedStatus,
                    true
                ) || filter.selectedStatus.equals("All", true))
                        && (it.dueDate == filter.selectedDueDate || filter.selectedDueDate.isEmpty())

            }

        val i = ""
        _subTasks.postValue(filtered)
        _subTasksForStatus.postValue(filtered)
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onApplySearch(filter: LocalEvents.ApplySearchOnSubTask) {
        if (filter.query?.isEmpty() == true) {
            _subTasks.postValue(originalSubTasks)
            _subTasksForStatus.postValue(originalSubTasks)
            return
        }
        if (filter.query != null) {
            val filtered =
                originalSubTasks.filter {
                    it.title.contains(filter.query, true)
                }
            _subTasks.postValue(filtered)
//            _subTasksForStatus.postValue(filtered)
        }
        else {
            alert("Unable to search")
        }
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun resetFilters(event: LocalEvents.ClearSubtaskFilters) {
        _subTasks.postValue(originalSubTasks)
        _subTasksForStatus.postValue(originalSubTasks)
    }

    override fun onCleared() {
        super.onCleared()
        EventBus.getDefault().unregister(this)
    }
}