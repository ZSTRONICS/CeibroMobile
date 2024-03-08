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
import com.zstronics.ceibro.CeibroApplication
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.database.dao.DrawingPinsV2Dao
import com.zstronics.ceibro.data.database.dao.TaskV2Dao
import com.zstronics.ceibro.data.database.models.tasks.CeibroTaskV2
import com.zstronics.ceibro.data.remote.TaskRemoteDataSource
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.ui.tasks.task.TaskStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TaskFromMeVM @Inject constructor(
    override val viewState: TaskFromMeState,
    private val remoteTask: TaskRemoteDataSource,
    val sessionManager: SessionManager,
    val taskDao: TaskV2Dao,
    private val drawingPinsDao: DrawingPinsV2Dao,
) : HiltBaseViewModel<ITaskFromMe.State>(), ITaskFromMe.ViewModel {
    var user = sessionManager.getUser().value
    var selectedState: String = TaskStatus.UNREAD.name.lowercase()
    var disabledUnreadState: MutableLiveData<Boolean> = MutableLiveData()
    var firstStartOfFragment = true

    private val _unreadTasks: MutableLiveData<MutableList<CeibroTaskV2>> = MutableLiveData()
    val unreadTasks: MutableLiveData<MutableList<CeibroTaskV2>> = _unreadTasks
    var originalUnreadTasks: MutableList<CeibroTaskV2> = mutableListOf()

    private val _ongoingTasks: MutableLiveData<MutableList<CeibroTaskV2>> = MutableLiveData()
    val ongoingTasks: MutableLiveData<MutableList<CeibroTaskV2>> = _ongoingTasks
    var originalOngoingTasks: MutableList<CeibroTaskV2> = mutableListOf()

    private val _doneTasks: MutableLiveData<MutableList<CeibroTaskV2>> = MutableLiveData()
    val doneTasks: MutableLiveData<MutableList<CeibroTaskV2>> = _doneTasks
    var originalDoneTasks: MutableList<CeibroTaskV2> = mutableListOf()

    private val _allTasks: MutableLiveData<MutableList<CeibroTaskV2>> = MutableLiveData()
    val allTasks: MutableLiveData<MutableList<CeibroTaskV2>> = _allTasks
    var allOriginalTasks: MutableLiveData<MutableList<CeibroTaskV2>> = MutableLiveData()

    init {
        selectedState = TaskStatus.UNREAD.name.lowercase()
    }

    fun loadAllTasks(skeletonVisible: Boolean, taskRV: RecyclerView, callBack: () -> Unit) {
        launch {
            val allFromMeUnreadTasks =
                CeibroApplication.CookiesManager.fromMeUnreadTasks.value ?: mutableListOf()
            val allFromMeOngoingTasks =
                CeibroApplication.CookiesManager.fromMeOngoingTasks.value ?: mutableListOf()
            val allFromMeDoneTasks =
                CeibroApplication.CookiesManager.fromMeDoneTasks.value ?: mutableListOf()
            val allFromMeTasks = mutableListOf<CeibroTaskV2>()
            allFromMeTasks.addAll(allFromMeUnreadTasks)
            allFromMeTasks.addAll(allFromMeOngoingTasks)
            allFromMeTasks.addAll(allFromMeDoneTasks)

            if (allFromMeTasks.isNotEmpty()) {

                if (firstStartOfFragment) {
                    selectedState = if (allFromMeUnreadTasks.isNotEmpty()) {
                        TaskStatus.UNREAD.name.lowercase()
                    } else {
                        TaskStatus.ONGOING.name.lowercase()
                    }
                    firstStartOfFragment = false
                }
                if (allFromMeUnreadTasks.isEmpty()) {
                    disabledUnreadState.value = true
                    if (selectedState.equals(
                            TaskStatus.UNREAD.name.lowercase(),
                            true
                        )
                    ) {  //if unread state was selected then we have to change it because it is disabled now
                        selectedState = TaskStatus.ONGOING.name.lowercase()
                    }
                } else {
                    disabledUnreadState.value = false
                }

                allOriginalTasks.postValue(allFromMeTasks)
                originalUnreadTasks = allFromMeUnreadTasks
                originalOngoingTasks = allFromMeOngoingTasks
                originalDoneTasks = allFromMeDoneTasks

                _allTasks.postValue(allFromMeTasks)
                _unreadTasks.postValue(allFromMeUnreadTasks)
                _ongoingTasks.postValue(allFromMeOngoingTasks)
                _doneTasks.postValue(allFromMeDoneTasks)

                callBack.invoke()
            } else {

                val unreadTasks =
                    taskDao.getFromMeTasks(TaskStatus.UNREAD.name.lowercase()).toMutableList()
                val ongoingTasks =
                    taskDao.getFromMeTasks(TaskStatus.ONGOING.name.lowercase()).toMutableList()
                val doneTasks =
                    taskDao.getFromMeTasks(TaskStatus.DONE.name.lowercase()).toMutableList()
                val allTasksList = mutableListOf<CeibroTaskV2>()
                allTasksList.addAll(unreadTasks)
                allTasksList.addAll(ongoingTasks)
                allTasksList.addAll(doneTasks)

                CeibroApplication.CookiesManager.fromMeUnreadTasks.postValue(unreadTasks)
                CeibroApplication.CookiesManager.fromMeOngoingTasks.postValue(ongoingTasks)
                CeibroApplication.CookiesManager.fromMeDoneTasks.postValue(doneTasks)

                if (firstStartOfFragment) {
                    selectedState = if (unreadTasks.isNotEmpty()) {
                        TaskStatus.UNREAD.name.lowercase()
                    } else {
                        TaskStatus.ONGOING.name.lowercase()
                    }
                    firstStartOfFragment = false
                }
                if (unreadTasks.isEmpty()) {
                    disabledUnreadState.value = true
                    if (selectedState.equals(
                            TaskStatus.UNREAD.name.lowercase(),
                            true
                        )
                    ) {  //if unread state was selected then we have to change it because it is disabled now
                        selectedState = TaskStatus.ONGOING.name.lowercase()
                    }
                } else {
                    disabledUnreadState.value = false
                }

                allOriginalTasks.postValue(allTasksList)
                originalUnreadTasks = unreadTasks
                originalOngoingTasks = ongoingTasks
                originalDoneTasks = doneTasks

                _allTasks.postValue(allTasksList)
                _unreadTasks.postValue(unreadTasks)
                _ongoingTasks.postValue(ongoingTasks)
                _doneTasks.postValue(doneTasks)

                callBack.invoke()
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
                    (it.title != null && it.title.contains(query.trim(), true)) ||
                            it.description.contains(query.trim(), true) ||
                            it.taskUID.contains(query.trim(), true) ||
                            it.assignedToState.any { assignee ->
                                assignee.firstName.contains(
                                    query.trim(),
                                    true
                                ) || assignee.surName.contains(query.trim(), true)
                            }
                }.toMutableList()
            _unreadTasks.postValue(filteredTasks)
        } else if (selectedState.equals(TaskStatus.ONGOING.name.lowercase(), true)) {
            val filteredTasks =
                originalOngoingTasks.filter {
                    (it.title != null && it.title.contains(query.trim(), true)) ||
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
        } else if (selectedState.equals(TaskStatus.DONE.name.lowercase(), true)) {
            val filteredTasks =
                originalDoneTasks.filter {
                    (it.title!=null && it.title.contains(query.trim(), true)) ||
                            it.description.contains(query.trim(), true) ||
                            it.taskUID.contains(query.trim(), true) ||
                            it.assignedToState.any { assignee ->
                                assignee.firstName.contains(
                                    query.trim(),
                                    true
                                ) || assignee.surName.contains(query.trim(), true)
                            }
                }.toMutableList()
            _doneTasks.postValue(filteredTasks)
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
                    updateTaskCanceledInLocal(
                        response.data.data,
                        taskDao,
                        user?.id,
                        sessionManager,
                        drawingPinsDao
                    )
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