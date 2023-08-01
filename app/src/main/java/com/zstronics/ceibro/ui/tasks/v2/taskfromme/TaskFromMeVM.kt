package com.zstronics.ceibro.ui.tasks.v2.taskfromme

import android.content.Context
import android.os.Handler
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
import com.zstronics.ceibro.data.database.dao.TaskV2Dao
import com.zstronics.ceibro.data.database.models.tasks.CeibroTaskV2
import com.zstronics.ceibro.data.remote.TaskRemoteDataSource
import com.zstronics.ceibro.data.repos.task.models.TaskV2Response
import com.zstronics.ceibro.data.repos.task.models.TasksV2DatabaseEntity
import com.zstronics.ceibro.data.repos.task.models.v2.TaskDetailEvents
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.ui.tasks.task.TaskStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import koleton.api.hideSkeleton
import koleton.api.loadSkeleton
import javax.inject.Inject

@HiltViewModel
class TaskFromMeVM @Inject constructor(
    override val viewState: TaskFromMeState,
    private val remoteTask: TaskRemoteDataSource,
    private val sessionManager: SessionManager,
    private val taskDao: TaskV2Dao
) : HiltBaseViewModel<ITaskFromMe.State>(), ITaskFromMe.ViewModel {
    val user = sessionManager.getUser().value
    var selectedState: String = "unread"

    private val _unreadTasks: MutableLiveData<MutableList<CeibroTaskV2>> = MutableLiveData()
    val unreadTasks: MutableLiveData<MutableList<CeibroTaskV2>> = _unreadTasks
    var originalUnreadTasks: MutableList<CeibroTaskV2> = mutableListOf()

    private val _ongoingTasks: MutableLiveData<MutableList<CeibroTaskV2>> = MutableLiveData()
    val ongoingTasks: MutableLiveData<MutableList<CeibroTaskV2>> = _ongoingTasks
    var originalOngoingTasks: MutableList<CeibroTaskV2> = mutableListOf()

    private val _doneTasks: MutableLiveData<MutableList<CeibroTaskV2>> = MutableLiveData()
    val doneTasks: MutableLiveData<MutableList<CeibroTaskV2>> = _doneTasks
    var originalDoneTasks: MutableList<CeibroTaskV2> = mutableListOf()

    private val _allTasks: MutableLiveData<TaskV2Response.AllTasks> = MutableLiveData()
    val allTasks: MutableLiveData<TaskV2Response.AllTasks> = _allTasks
    var allOriginalTasks: MutableLiveData<TaskV2Response.AllTasks> = MutableLiveData()

    init {
        selectedState = "unread"
    }

    fun loadAllTasks(skeletonVisible: Boolean, taskRV: RecyclerView, callBack: () -> Unit) {
        launch {
            val taskLocalData = taskDao.getTasks("from-me")
            if (taskLocalData != null) {
                val allTasks = taskLocalData.allTasks
                val unreadTask = allTasks.unread
                val ongoingTask = allTasks.ongoing
                val doneTask = allTasks.done
                _unreadTasks.postValue(unreadTask as MutableList<CeibroTaskV2>?)
                _ongoingTasks.postValue(ongoingTask as MutableList<CeibroTaskV2>?)
                _doneTasks.postValue(doneTask as MutableList<CeibroTaskV2>?)
                _allTasks.postValue(allTasks)

                originalUnreadTasks = unreadTask
                originalOngoingTasks = ongoingTask
                originalDoneTasks = doneTask
                allOriginalTasks.postValue(allTasks)
                callBack.invoke()
            } else {
                if (skeletonVisible) {
                    taskRV.loadSkeleton(R.layout.layout_task_box_v2_for_skeleton) {
                        itemCount(5)
                        color(R.color.appGrey3)
                    }
                }
                when (val response = remoteTask.getAllTasks("from-me")) {
                    is ApiResponse.Success -> {

                        taskDao.insertTaskData(
                            TasksV2DatabaseEntity(
                                rootState = "from-me",
                                allTasks = response.data.allTasks
                            )
                        )

                        val unreadTask = response.data.allTasks.unread
                        val ongoingTask = response.data.allTasks.ongoing
                        val doneTask = response.data.allTasks.done
                        val allTasks = response.data.allTasks

                        _unreadTasks.postValue(unreadTask as MutableList<CeibroTaskV2>?)
                        _ongoingTasks.postValue(ongoingTask as MutableList<CeibroTaskV2>?)
                        _doneTasks.postValue(doneTask as MutableList<CeibroTaskV2>?)
                        _allTasks.postValue(allTasks)

                        originalUnreadTasks = unreadTask
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
            }
        }
    }

    fun searchTasks(query: String) {
        if (query.isEmpty()) {
            _allTasks.postValue(allOriginalTasks.value)
            _unreadTasks.postValue(originalUnreadTasks)
            _ongoingTasks.postValue(originalOngoingTasks)
            _doneTasks.postValue(originalDoneTasks)
            return
        }
        if (selectedState.equals(TaskStatus.UNREAD.name.lowercase(), true)) {
            val filteredTasks =
                originalUnreadTasks.filter {
                    (it.topic != null && it.topic.topic.contains(query.trim(), true)) ||
                            it.description.contains(query.trim(), true) ||
                            it.taskUID.contains(query.trim(), true) ||
                            it.assignedToState.any { assignee ->
                                assignee.firstName.contains(
                                    query.trim(),
                                    true
                                ) || assignee.surName.contains(query.trim(), true)
                            } ||
                            it.events.filter { events ->
                                events.eventType.equals(
                                    TaskDetailEvents.Comment.eventValue,
                                    true
                                )
                            }.any { filteredComments ->
                                filteredComments.commentData?.message?.contains(
                                    query,
                                    true
                                ) == true
                            }
                }
            _unreadTasks.postValue(filteredTasks as MutableList<CeibroTaskV2>?)
        } else if (selectedState.equals(TaskStatus.ONGOING.name.lowercase(), true)) {
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
                            } ||
                            it.events.filter { events ->
                                events.eventType.equals(
                                    TaskDetailEvents.Comment.eventValue,
                                    true
                                )
                            }.any { filteredComments ->
                                filteredComments.commentData?.message?.contains(
                                    query,
                                    true
                                ) == true
                            }
                }
            _ongoingTasks.postValue(filteredTasks as MutableList<CeibroTaskV2>?)
        } else if (selectedState.equals(TaskStatus.DONE.name.lowercase(), true)) {
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
                            } ||
                            it.events.filter { events ->
                                events.eventType.equals(
                                    TaskDetailEvents.Comment.eventValue,
                                    true
                                )
                            }.any { filteredComments ->
                                filteredComments.commentData?.message?.contains(
                                    query,
                                    true
                                ) == true
                            }
                }
            _doneTasks.postValue(filteredTasks as MutableList<CeibroTaskV2>?)
        }
    }


    fun showCancelTaskDialog(context: Context, taskData: CeibroTaskV2, callBack: () -> Unit) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.layout_custom_dialog, null)

        val builder: AlertDialog.Builder = AlertDialog.Builder(context).setView(view)
        val alertDialog = builder.create()

        val yesBtn = view.findViewById<Button>(R.id.yesBtn)
        val noBtn = view.findViewById<Button>(R.id.noBtn)
        val dialogText = view.findViewById<TextView>(R.id.dialog_text)
        dialogText.text = context.resources.getString(R.string.do_you_want_to_cancel_the_task)
        alertDialog.window?.setBackgroundDrawable(null)
        alertDialog.show()

        yesBtn.setOnClickListener {
            cancelTask(taskData.id) { isSuccess ->
                alertDialog.dismiss()
            }
        }

        noBtn.setOnClickListener {
            alertDialog.dismiss()
        }
    }



    private fun cancelTask(taskId: String, callBack: (isSuccess: Boolean) -> Unit) {
        launch {
            loading(true)
            when (val response = remoteTask.cancelTask(taskId)) {
                is ApiResponse.Success -> {
                    updateTaskCanceledInLocal(response.data.data, taskDao, user?.id, sessionManager)
                    val handler = Handler()
                    handler.postDelayed({
                        loading(false, "")
                        callBack.invoke(true)
                    }, 50)
                }
                is ApiResponse.Error -> {
                    loading(false, response.error.message)
                    callBack.invoke(false)
                }
            }
        }
    }

    fun saveFromMeUnread(isUnread: Boolean) {
        sessionManager.saveFromMeUnread(isUnread)
    }

}