package com.zstronics.ceibro.ui.tasks.v2.taskfromme

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.database.models.tasks.CeibroTask
import com.zstronics.ceibro.data.database.models.tasks.CeibroTaskV2
import com.zstronics.ceibro.data.remote.TaskRemoteDataSource
import com.zstronics.ceibro.data.repos.projects.projectsmain.AllProjectsResponse
import com.zstronics.ceibro.data.repos.projects.projectsmain.ProjectsWithMembersResponse
import com.zstronics.ceibro.data.repos.task.models.TaskV2Response
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TaskFromMeVM @Inject constructor(
    override val viewState: TaskFromMeState,
    private val remoteTask: TaskRemoteDataSource
) : HiltBaseViewModel<ITaskFromMe.State>(), ITaskFromMe.ViewModel {
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

    fun loadAllTasks(callBack: () -> Unit) {
        launch {
            when (val response = remoteTask.getAllTasks("from-me")) {
                is ApiResponse.Success -> {
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
                    callBack.invoke()
                }
                is ApiResponse.Error -> {
                    alert(response.error.message)
                    callBack.invoke()
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
        if (selectedState.equals("unread", true)) {
            val filteredTasks =
                originalUnreadTasks.filter {
                    (it.topic != null && it.topic.topic.contains(query.trim(), true)) ||
                            it.description.contains(query.trim(), true)
                }
            _unreadTasks.postValue(filteredTasks as MutableList<CeibroTaskV2>?)
        }
        else if (selectedState.equals("ongoing", true)) {
            val filteredTasks =
                originalOngoingTasks.filter {
                    (it.topic != null && it.topic.topic.contains(query.trim(), true)) ||
                            it.description.contains(query.trim(), true)
                }
            _ongoingTasks.postValue(filteredTasks as MutableList<CeibroTaskV2>?)
        }
        else if (selectedState.equals("done", true)) {
            val filteredTasks =
                originalDoneTasks.filter {
                    (it.topic != null && it.topic.topic.contains(query.trim(), true)) ||
                            it.description.contains(query.trim(), true)
                }
            _doneTasks.postValue(filteredTasks as MutableList<CeibroTaskV2>?)
        }
    }

}