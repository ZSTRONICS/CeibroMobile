package com.zstronics.ceibro.ui.tasks.taskdetailview

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.PopupWindow
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.database.models.subtask.AllSubtask
import com.zstronics.ceibro.data.database.models.tasks.CeibroTask
import com.zstronics.ceibro.data.repos.task.ITaskRepository
import com.zstronics.ceibro.data.repos.task.models.UpdateSubTaskStatusRequest
import com.zstronics.ceibro.data.repos.task.models.UpdateSubTaskStatusWithoutCommentRequest
import com.zstronics.ceibro.ui.tasks.subtask.SubTaskStatus
import com.zstronics.ceibro.ui.tasks.task.TaskStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TaskDetailVM @Inject constructor(
    override val viewState: TaskDetailState,
    private val taskRepository: ITaskRepository,
) : HiltBaseViewModel<ITaskDetail.State>(), ITaskDetail.ViewModel {
    private val _task: MutableLiveData<CeibroTask> = MutableLiveData()
    val task: LiveData<CeibroTask> = _task

    private val _subTasks: MutableLiveData<List<AllSubtask>> = MutableLiveData()
    val subTasks: LiveData<List<AllSubtask>> = _subTasks

    override fun onFirsTimeUiCreate(bundle: Bundle?) {
        super.onFirsTimeUiCreate(bundle)
        val taskParcel: CeibroTask? = bundle?.getParcelable("task")
        _task.value = taskParcel

        taskParcel?._id?.let { getSubTasks(it) }
    }

    override fun getSubTasks(taskId: String) {
        launch {
            _subTasks.postValue(taskRepository.getSubTaskByTaskId(taskId))
        }
    }

    override fun showSubtaskCardMenuPopup(v: View) {
        val popUpWindowObj = popUpMenu(v)
        popUpWindowObj.showAsDropDown(v.findViewById(R.id.subTaskMoreMenuBtn), 0, 10)
    }

    override fun popUpMenu(v: View): PopupWindow {
        val popupWindow = PopupWindow(v.context)
        val context: Context = v.context
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.layout_subtask_card_menu, null)

        val editDetails = view.findViewById<View>(R.id.editDetails)
        val closeSubtask = view.findViewById<View>(R.id.closeSubtask)

        editDetails.setOnClickListener {
            clickEvent?.postValue(115)
            popupWindow.dismiss()
        }
        closeSubtask.setOnClickListener {
            clickEvent?.postValue(116)
            popupWindow.dismiss()
        }

        popupWindow.isFocusable = true
        popupWindow.width = WindowManager.LayoutParams.WRAP_CONTENT
        popupWindow.height = WindowManager.LayoutParams.WRAP_CONTENT
        popupWindow.contentView = view
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        popupWindow.elevation = 13f
        return popupWindow
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
            }
            else {
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
            }
            else {
                callBack.invoke(result)
            }
        }
    }
}