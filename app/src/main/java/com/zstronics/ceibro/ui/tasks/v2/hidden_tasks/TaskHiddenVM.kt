package com.zstronics.ceibro.ui.tasks.v2.hidden_tasks

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.base.CookiesManager
import com.zstronics.ceibro.data.database.dao.TaskV2Dao
import com.zstronics.ceibro.data.database.models.tasks.CeibroTaskV2
import com.zstronics.ceibro.data.remote.TaskRemoteDataSource
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.ui.tasks.task.TaskStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TaskHiddenVM @Inject constructor(
    override val viewState: TaskHiddenState,
    private val remoteTask: TaskRemoteDataSource,
    private val sessionManager: SessionManager,
    private val taskDao: TaskV2Dao
) : HiltBaseViewModel<ITaskHidden.State>(), ITaskHidden.ViewModel {
    val user = sessionManager.getUser().value
    var selectedState: String = "ongoing"

    private val _cancelledTasks: MutableLiveData<MutableList<CeibroTaskV2>> = MutableLiveData()
    val cancelledTasks: MutableLiveData<MutableList<CeibroTaskV2>> = _cancelledTasks
    var originalCancelledTasks: MutableList<CeibroTaskV2> = mutableListOf()

    private val _ongoingTasks: MutableLiveData<MutableList<CeibroTaskV2>> = MutableLiveData()
    val ongoingTasks: MutableLiveData<MutableList<CeibroTaskV2>> = _ongoingTasks
    var originalOngoingTasks: MutableList<CeibroTaskV2> = mutableListOf()

    private val _doneTasks: MutableLiveData<MutableList<CeibroTaskV2>> = MutableLiveData()
    val doneTasks: MutableLiveData<MutableList<CeibroTaskV2>> = _doneTasks
    var originalDoneTasks: MutableList<CeibroTaskV2> = mutableListOf()

    private val _allTasks: MutableLiveData<MutableList<CeibroTaskV2>> = MutableLiveData()
    val allTasks: MutableLiveData<MutableList<CeibroTaskV2>> = _allTasks
    var allOriginalTasks: MutableLiveData<MutableList<CeibroTaskV2>> = MutableLiveData()

    fun loadAllTasks(skeletonVisible: Boolean, taskRV: RecyclerView, callBack: () -> Unit) {
        launch {
            val allHiddenCanceledTasks = CookiesManager.hiddenCanceledTasks.value ?: mutableListOf()
            val allHiddenOngoingTasks = CookiesManager.hiddenOngoingTasks.value ?: mutableListOf()
            val allHiddenDoneTasks = CookiesManager.hiddenDoneTasks.value ?: mutableListOf()
            val allHiddenTasks = mutableListOf<CeibroTaskV2>()
            allHiddenTasks.addAll(allHiddenCanceledTasks)
            allHiddenTasks.addAll(allHiddenOngoingTasks)
            allHiddenTasks.addAll(allHiddenDoneTasks)

            if (allHiddenTasks.isNotEmpty()) {
                _cancelledTasks.postValue(allHiddenCanceledTasks)
                _ongoingTasks.postValue(allHiddenOngoingTasks)
                _doneTasks.postValue(allHiddenDoneTasks)
                _allTasks.postValue(allHiddenTasks)

                originalCancelledTasks = allHiddenCanceledTasks
                originalOngoingTasks = allHiddenOngoingTasks
                originalDoneTasks = allHiddenDoneTasks
                allOriginalTasks.postValue(allHiddenTasks)
                callBack.invoke()
            } else {

                val canceledTasks = taskDao.getHiddenTasks(TaskStatus.CANCELED.name.lowercase()).toMutableList()
                val ongoingTasks = taskDao.getHiddenTasks(TaskStatus.ONGOING.name.lowercase()).toMutableList()
                val doneTasks = taskDao.getHiddenTasks(TaskStatus.DONE.name.lowercase()).toMutableList()
                val allTasksList = mutableListOf<CeibroTaskV2>()
                allTasksList.addAll(canceledTasks)
                allTasksList.addAll(ongoingTasks)
                allTasksList.addAll(doneTasks)

                CookiesManager.hiddenCanceledTasks.postValue(canceledTasks)
                CookiesManager.hiddenOngoingTasks.postValue(ongoingTasks)
                CookiesManager.hiddenDoneTasks.postValue(doneTasks)

                _cancelledTasks.postValue(canceledTasks)
                _ongoingTasks.postValue(ongoingTasks)
                _doneTasks.postValue(doneTasks)
                _allTasks.postValue(allTasksList)

                originalCancelledTasks = canceledTasks
                originalOngoingTasks = ongoingTasks
                originalDoneTasks = doneTasks
                allOriginalTasks.postValue(allTasksList)
                callBack.invoke()
            }


            /*val taskLocalData = TaskV2DaoHelper(taskDao).getTasks(TaskRootStateTags.Hidden.tagValue)
            if (!TaskV2DaoHelper(taskDao).isTaskListEmpty(TaskRootStateTags.Hidden.tagValue, taskLocalData)) {
                val allTasks = taskLocalData.allTasks
                val canceled = allTasks.canceled.sortedByDescending { it.updatedAt }.toMutableList()
                val ongoingTask =
                    allTasks.ongoing.sortedByDescending { it.updatedAt }.toMutableList()
                val doneTask = allTasks.done.sortedByDescending { it.updatedAt }.toMutableList()
                _ongoingTasks.postValue(ongoingTask)
                _doneTasks.postValue(doneTask)
                _cancelledTasks.postValue(canceled)
                _allTasks.postValue(allTasks)

                originalCancelledTasks = canceled
                originalOngoingTasks = ongoingTask
                originalDoneTasks = doneTask
                allOriginalTasks.postValue(allTasks)
                callBack.invoke()
            }
            else {
                if (skeletonVisible) {
                    taskRV.loadSkeleton(R.layout.layout_task_box_v2_for_skeleton) {
                        itemCount(5)
                        color(R.color.appGrey3)
                    }
                }
                when (val response = remoteTask.getAllTasks(TaskRootStateTags.Hidden.tagValue)) {
                    is ApiResponse.Success -> {
                        TaskV2DaoHelper(taskDao).insertTaskData(
                            TasksV2DatabaseEntity(
                                rootState = TaskRootStateTags.Hidden.tagValue,
                                allTasks = response.data.allTasks
                            )
                        )
                        val ongoingTask =
                            response.data.allTasks.ongoing.sortedByDescending { it.updatedAt }
                                .toMutableList()
                        val doneTask =
                            response.data.allTasks.done.sortedByDescending { it.updatedAt }
                                .toMutableList()
                        val allTasks = response.data.allTasks
                        val canceled =
                            response.data.allTasks.canceled.sortedByDescending { it.updatedAt }
                                .toMutableList()
                        _cancelledTasks.postValue(canceled)
                        _ongoingTasks.postValue(ongoingTask)
                        _doneTasks.postValue(doneTask)
                        _allTasks.postValue(allTasks)

                        originalCancelledTasks = canceled
                        originalOngoingTasks = ongoingTask
                        originalDoneTasks = doneTask
                        allOriginalTasks.postValue(allTasks)

                        if (skeletonVisible) {
                            taskRV.hideSkeleton()
                        }
                        callBack.invoke()
                    }

                    is ApiResponse.Error -> {
                        alert(response.error.message)
                        if (skeletonVisible) {
                            taskRV.hideSkeleton()
                        }
                        callBack.invoke()
                    }
                }
            }*/
        }
    }

    fun searchTasks(query: String) {
        if (query.isEmpty()) {
            _allTasks.postValue(allOriginalTasks.value)
            _cancelledTasks.postValue(originalCancelledTasks)
            _ongoingTasks.postValue(originalOngoingTasks)
            _doneTasks.postValue(originalDoneTasks)
            return
        }
        if (selectedState.equals("canceled", true)) {
            val filteredTasks =
                originalCancelledTasks.filter {
                    (it.topic != null && it.topic.topic.contains(query.trim(), true)) ||
                            it.description.contains(query.trim(), true) ||
                            it.taskUID.contains(query.trim(), true) ||
                            it.assignedToState.any { assignee ->
                                assignee.firstName.contains(
                                    query.trim(),
                                    true
                                ) || assignee.surName.contains(query.trim(), true)
                            }
                }.toMutableList()
            _cancelledTasks.postValue(filteredTasks)
        } else if (selectedState.equals("ongoing", true)) {
            val filteredTasks =
                originalOngoingTasks.filter {
                    (it.topic != null && it.topic.topic.contains(query.trim(), true)) ||
                            it.description.contains(query.trim(), true) ||
                            it.taskUID.contains(query.trim(), true) ||
                            it.assignedToState.any { assignee ->
                                assignee.firstName.contains(
                                    query.trim(),
                                    true
                                ) || assignee.surName.contains(query.trim(), true)
                            }
                }.toMutableList()
            _ongoingTasks.postValue(filteredTasks)
        } else if (selectedState.equals("done", true)) {
            val filteredTasks =
                originalDoneTasks.filter {
                    (it.topic != null && it.topic.topic.contains(query.trim(), true)) ||
                            it.description.contains(query.trim(), true) ||
                            it.taskUID.contains(query.trim(), true) ||
                            it.assignedToState.any { assignee ->
                                assignee.firstName.contains(
                                    query.trim(),
                                    true
                                ) || assignee.surName.contains(query.trim(), true)
                            }
//                            it.events.filter { events ->
//                                events.eventType.equals(
//                                    TaskDetailEvents.Comment.eventValue,
//                                    true
//                                )
//                            }.any { filteredComments ->
//                                filteredComments.commentData?.message?.contains(
//                                    query,
//                                    true
//                                ) == true
//                            }
                }.toMutableList()
            _doneTasks.postValue(filteredTasks)
        }
    }


    fun showUnHideTaskDialog(context: Context, taskData: CeibroTaskV2) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.layout_custom_dialog, null)

        val builder: AlertDialog.Builder = AlertDialog.Builder(context).setView(view)
        val alertDialog = builder.create()

        val yesBtn = view.findViewById<Button>(R.id.yesBtn)
        val noBtn = view.findViewById<Button>(R.id.noBtn)
        val dialogText = view.findViewById<TextView>(R.id.dialog_text)
        dialogText.text = context.resources.getString(R.string.do_you_want_to_un_hide_the_task)
        alertDialog.window?.setBackgroundDrawable(null)
        alertDialog.show()

        yesBtn.setOnClickListener {
            unHideTask(taskData.id) { isSuccess ->
                alertDialog.dismiss()
            }
        }

        noBtn.setOnClickListener {
            alertDialog.dismiss()
        }
    }

    fun showUnCancelTaskDialog(context: Context, taskData: CeibroTaskV2) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.layout_custom_dialog, null)

        val builder: AlertDialog.Builder = AlertDialog.Builder(context).setView(view)
        val alertDialog = builder.create()

        val yesBtn = view.findViewById<Button>(R.id.yesBtn)
        val noBtn = view.findViewById<Button>(R.id.noBtn)
        val dialogText = view.findViewById<TextView>(R.id.dialog_text)
        dialogText.text = context.resources.getString(R.string.do_you_want_to_un_cancel_the_task)
        alertDialog.window?.setBackgroundDrawable(null)
        alertDialog.show()

        yesBtn.setOnClickListener {
            unCancelTask(taskData.id) {
                alertDialog.dismiss()
            }
        }

        noBtn.setOnClickListener {
            alertDialog.dismiss()
        }
    }

    private fun unCancelTask(taskId: String, callBack: (isSuccess: Boolean) -> Unit) {
        launch {
            loading(true)
            when (val response = remoteTask.unCancelTask(taskId)) {
                is ApiResponse.Success -> {
                    launch {
                        updateTaskUnCanceledInLocal(
                            response.data.data,
                            taskDao,
                            sessionManager
                        )
                    }
                    val handler = Handler(Looper.getMainLooper())
                    handler.postDelayed({
                        loading(false, "")
                        callBack.invoke(true)
                    }, 30)
                }

                is ApiResponse.Error -> {
                    loading(false, response.error.message)
                    callBack.invoke(false)
                }
            }
        }
    }

    private fun unHideTask(taskId: String, callBack: (isSuccess: Boolean) -> Unit) {
        launch {
            loading(true)
            when (val response = remoteTask.unHideTask(taskId)) {
                is ApiResponse.Success -> {
                    val unHideResponse = response.data
                    updateTaskUnHideInLocal(unHideResponse, taskDao, sessionManager)
                    loading(false, "")
                    callBack.invoke(true)
                }

                is ApiResponse.Error -> {
                    loading(false, response.error.message)
                    callBack.invoke(false)
                }
            }
        }
    }

    fun saveHiddenUnread(isUnread: Boolean) {
        sessionManager.saveHiddenUnread(isUnread)
    }

}