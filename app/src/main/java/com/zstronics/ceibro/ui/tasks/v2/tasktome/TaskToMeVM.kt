package com.zstronics.ceibro.ui.tasks.v2.tasktome

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
import dagger.hilt.android.lifecycle.HiltViewModel
import koleton.api.hideSkeleton
import koleton.api.loadSkeleton
import javax.inject.Inject

@HiltViewModel
class TaskToMeVM @Inject constructor(
    override val viewState: TaskToMeState,
    private val remoteTask: TaskRemoteDataSource,
    private val taskDao: TaskV2Dao
) : HiltBaseViewModel<ITaskToMe.State>(), ITaskToMe.ViewModel {
    var selectedState: String = "new"

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

    init {
        selectedState = "new"
    }

    fun loadAllTasks(skeletonVisible: Boolean, taskRV: RecyclerView, callBack: () -> Unit) {
        launch {
            val taskLocalData = taskDao.getTasks("to-me")
            if (taskLocalData != null) {
                val allTasks = taskLocalData.allTasks
                val newTask = allTasks.new
                val ongoingTask = allTasks.ongoing
                val doneTask = allTasks.done

                _newTasks.postValue(newTask as MutableList<CeibroTaskV2>?)
                _ongoingTasks.postValue(ongoingTask as MutableList<CeibroTaskV2>?)
                _doneTasks.postValue(doneTask as MutableList<CeibroTaskV2>?)
                _allTasks.postValue(allTasks)

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

                        _newTasks.postValue(newTask as MutableList<CeibroTaskV2>?)
                        _ongoingTasks.postValue(ongoingTask as MutableList<CeibroTaskV2>?)
                        _doneTasks.postValue(doneTask as MutableList<CeibroTaskV2>?)
                        _allTasks.postValue(allTasks)

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
                            it.description.contains(query.trim(), true)
                }
            _newTasks.postValue(filteredTasks as MutableList<CeibroTaskV2>?)
        } else if (selectedState.equals("ongoing", true)) {
            val filteredTasks =
                originalOngoingTasks.filter {
                    (it.topic != null && it.topic.topic.contains(query.trim(), true)) ||
                            it.description.contains(query.trim(), true)
                }
            _ongoingTasks.postValue(filteredTasks as MutableList<CeibroTaskV2>?)
        } else if (selectedState.equals("done", true)) {
            val filteredTasks =
                originalDoneTasks.filter {
                    (it.topic != null && it.topic.topic.contains(query.trim(), true)) ||
                            it.description.contains(query.trim(), true)
                }
            _doneTasks.postValue(filteredTasks as MutableList<CeibroTaskV2>?)
        }
    }

}