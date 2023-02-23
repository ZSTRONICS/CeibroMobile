package com.zstronics.ceibro.ui.tasks.subtask

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.database.models.subtask.AllSubtask
import com.zstronics.ceibro.data.database.models.tasks.CeibroTask
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
                haveMembers(
                    it.assignedTo[0].members,
                    filter.assigneeToMembers
                ) || (it.dueDate ==
                        filter.selectedDueDate && filter.selectedDueDate.isNotEmpty())

            }
        _subTasks.postValue(filtered)
    }

    private fun haveMembers(
        list: List<TaskMember>,
        assigneeToMembers: List<TaskMember>?
    ): Boolean {
        // Return true if assigneeToMembers is null or empty
        if (assigneeToMembers == null || assigneeToMembers.isEmpty()) {
            return true
        }

        // Check if the assigneeToMembers id is found in the list
        return list.any { it.id == assigneeToMembers[0].id }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun resetFilters(event: LocalEvents.ClearSubtaskFilters) {
        _subTasks.postValue(originalSubTasks)
    }

    override fun onCleared() {
        super.onCleared()
        EventBus.getDefault().unregister(this)
    }
}