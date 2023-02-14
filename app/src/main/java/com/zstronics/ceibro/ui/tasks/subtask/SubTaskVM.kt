package com.zstronics.ceibro.ui.tasks.subtask

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
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
import com.zstronics.ceibro.data.repos.task.TaskRepository
import com.zstronics.ceibro.data.repos.task.models.UpdateSubTaskStatusRequest
import com.zstronics.ceibro.data.repos.task.models.UpdateSubTaskStatusWithoutCommentRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SubTaskVM @Inject constructor(
    override val viewState: SubTaskState,
    val taskRepository: TaskRepository
) : HiltBaseViewModel<ISubTask.State>(), ISubTask.ViewModel {
    private val _subTasks: MutableLiveData<List<AllSubtask>> = MutableLiveData()
    val subTasks: LiveData<List<AllSubtask>> = _subTasks

    init {
        getSubTasks()
    }

    override fun getSubTasks() {
        launch {
            _subTasks.postValue(taskRepository.getAllSubtasks())
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
        val deleteSubtask = view.findViewById<View>(R.id.deleteSubtask)

        editDetails.setOnClickListener {
            clickEvent?.postValue(115)
            popupWindow.dismiss()
        }
        deleteSubtask.setOnClickListener {
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
}