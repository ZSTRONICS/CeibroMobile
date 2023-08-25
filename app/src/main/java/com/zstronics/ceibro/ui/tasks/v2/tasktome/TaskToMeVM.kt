package com.zstronics.ceibro.ui.tasks.v2.tasktome

import android.content.Context
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
class TaskToMeVM @Inject constructor(
    override val viewState: TaskToMeState,
    private val remoteTask: TaskRemoteDataSource,
    private val sessionManager: SessionManager,
    private val taskDao: TaskV2Dao
) : HiltBaseViewModel<ITaskToMe.State>(), ITaskToMe.ViewModel {
    val user = sessionManager.getUser().value
    var selectedState: String = TaskStatus.NEW.name.lowercase()
    var disabledNewState: MutableLiveData<Boolean> = MutableLiveData()
    var firstStartOfFragment = true

    private val _newTasks: MutableLiveData<MutableList<CeibroTaskV2>> = MutableLiveData()
    val newTasks: MutableLiveData<MutableList<CeibroTaskV2>> = _newTasks
    var originalNewTasks: MutableList<CeibroTaskV2> = mutableListOf()

    private val _ongoingTasks: MutableLiveData<MutableList<CeibroTaskV2>> = MutableLiveData()
    val ongoingTasks: MutableLiveData<MutableList<CeibroTaskV2>> = _ongoingTasks
    var originalOngoingTasks: MutableList<CeibroTaskV2> = mutableListOf()

    private val _doneTasks: MutableLiveData<MutableList<CeibroTaskV2>> = MutableLiveData()
    val doneTasks: MutableLiveData<MutableList<CeibroTaskV2>> = _doneTasks
    var originalDoneTasks: MutableList<CeibroTaskV2> = mutableListOf()

    private val _allTasks: MutableLiveData<TaskV2Response.AllTasks> = MutableLiveData()
    val allTasks: MutableLiveData<TaskV2Response.AllTasks> = _allTasks
    var allOriginalTasks: MutableLiveData<TaskV2Response.AllTasks> = MutableLiveData()
    val localSearchQuery: MutableLiveData<String> = MutableLiveData("")

    init {
        selectedState = TaskStatus.NEW.name.lowercase()
    }

    fun loadAllTasks(skeletonVisible: Boolean, taskRV: RecyclerView, callBack: () -> Unit) {
        launch {
            val taskLocalData = taskDao.getTasks("to-me")
            if (taskLocalData != null) {
                val allTasks = taskLocalData.allTasks
                val newTask = allTasks.new.sortedByDescending { it.updatedAt }
                val ongoingTask = allTasks.ongoing.sortedByDescending { it.updatedAt }
                val doneTask = allTasks.done.sortedByDescending { it.updatedAt }

                if (firstStartOfFragment) {
                    selectedState = if (newTask.isNotEmpty()) {
                        TaskStatus.NEW.name.lowercase()
                    } else {
                        TaskStatus.ONGOING.name.lowercase()
                    }
                    firstStartOfFragment = false
                }
                if (newTask.isEmpty()) {
                    disabledNewState.value = true
                    if (selectedState.equals(
                            TaskStatus.NEW.name.lowercase(),
                            true
                        )
                    ) {  //if new state was selected then we have to change it because it is disabled now
                        selectedState = TaskStatus.ONGOING.name.lowercase()
                    }
                } else {
                    disabledNewState.value = false
                }
                _allTasks.postValue(allTasks)
                _newTasks.postValue(newTask as MutableList<CeibroTaskV2>?)
                _ongoingTasks.postValue(ongoingTask as MutableList<CeibroTaskV2>?)
                _doneTasks.postValue(doneTask as MutableList<CeibroTaskV2>?)

                originalNewTasks = newTask
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
                when (val response = remoteTask.getAllTasks("to-me")) {
                    is ApiResponse.Success -> {
                        taskDao.insertTaskData(
                            TasksV2DatabaseEntity(
                                rootState = "to-me",
                                allTasks = response.data.allTasks
                            )
                        )
                        val newTask = response.data.allTasks.new
                        val ongoingTask = response.data.allTasks.ongoing
                        val doneTask = response.data.allTasks.done
                        val allTasks = response.data.allTasks

                        if (firstStartOfFragment) {
                            selectedState = if (newTask.isNotEmpty()) {
                                TaskStatus.NEW.name.lowercase()
                            } else {
                                TaskStatus.ONGOING.name.lowercase()
                            }
                            firstStartOfFragment = false
                        }
                        if (newTask.isEmpty()) {
                            disabledNewState.value = true
                            if (selectedState.equals(
                                    TaskStatus.NEW.name.lowercase(),
                                    true
                                )
                            ) {  //if new state was selected then we have to change it because it is disabled now
                                selectedState = TaskStatus.ONGOING.name.lowercase()
                            }
                        } else {
                            disabledNewState.value = false
                        }
                        _allTasks.postValue(allTasks)
                        _newTasks.postValue(newTask as MutableList<CeibroTaskV2>?)
                        _ongoingTasks.postValue(ongoingTask as MutableList<CeibroTaskV2>?)
                        _doneTasks.postValue(doneTask as MutableList<CeibroTaskV2>?)

                        originalNewTasks = newTask
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
            _newTasks.postValue(originalNewTasks)
            _ongoingTasks.postValue(originalOngoingTasks)
            _doneTasks.postValue(originalDoneTasks)
            return
        }
        if (selectedState.equals("new", true)) {
            val filteredTasks =
                originalNewTasks.filter {
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
            _newTasks.postValue(filteredTasks as MutableList<CeibroTaskV2>?)
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


    fun showHideTaskDialog(context: Context, taskData: CeibroTaskV2) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.layout_custom_dialog, null)

        val builder: AlertDialog.Builder = AlertDialog.Builder(context).setView(view)
        val alertDialog = builder.create()

        val yesBtn = view.findViewById<Button>(R.id.yesBtn)
        val noBtn = view.findViewById<Button>(R.id.noBtn)
        val dialogText = view.findViewById<TextView>(R.id.dialog_text)
        dialogText.text = context.resources.getString(R.string.do_you_want_to_hide_the_task)
        alertDialog.window?.setBackgroundDrawable(null)
        alertDialog.show()

        yesBtn.setOnClickListener {
            hideTask(taskData.id) { isSuccess ->
                alertDialog.dismiss()
            }
        }

        noBtn.setOnClickListener {
            alertDialog.dismiss()
        }
    }


    private fun hideTask(taskId: String, callBack: (isSuccess: Boolean) -> Unit) {
        launch {
            loading(true)
            when (val response = remoteTask.hideTask(taskId)) {
                is ApiResponse.Success -> {
                    val hideResponse = response.data
                    updateTaskHideInLocal(hideResponse, taskDao, user?.id)
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

    fun saveToMeUnread(isUnread: Boolean) {
        sessionManager.saveToMeUnread(isUnread)
    }

}