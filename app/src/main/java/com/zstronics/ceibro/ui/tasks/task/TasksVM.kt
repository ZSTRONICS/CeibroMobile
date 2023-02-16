package com.zstronics.ceibro.ui.tasks.task

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.PopupWindow
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.database.models.tasks.CeibroTask
import com.zstronics.ceibro.data.database.models.tasks.TaskMember
import com.zstronics.ceibro.data.repos.task.TaskRepository
import com.zstronics.ceibro.data.repos.task.models.UpdateTaskRequestNoAdvanceOptions
import com.zstronics.ceibro.data.sessions.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TasksVM @Inject constructor(
    override val viewState: TasksState,
    val sessionManager: SessionManager,
    private val taskRepository: TaskRepository
) : HiltBaseViewModel<ITasks.State>(), ITasks.ViewModel {
    val user = sessionManager.getUser().value

    private val _tasks: MutableLiveData<List<CeibroTask>> = MutableLiveData()
    val tasks: LiveData<List<CeibroTask>> = _tasks

    init {
        getTasks()
    }

    override fun getTasks() {
        launch {
            _tasks.postValue(taskRepository.tasks().reversed())
        }
    }

    fun deleteTask(taskId: String) {
        launch {
            loading(true)
            taskRepository.deleteTask(taskId) { isSuccess, message ->
                if (isSuccess) {
                    loading(false, "Task Deleted Successfully")
                    getTasks()
                } else {
                    loading(false, message)
                }
            }
        }
    }


}