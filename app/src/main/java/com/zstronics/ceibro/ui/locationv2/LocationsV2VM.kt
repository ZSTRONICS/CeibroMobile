package com.zstronics.ceibro.ui.locationv2

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.database.dao.DownloadedDrawingV2Dao
import com.zstronics.ceibro.data.database.dao.DrawingPinsV2Dao
import com.zstronics.ceibro.data.database.dao.GroupsV2Dao
import com.zstronics.ceibro.data.database.dao.TaskV2Dao
import com.zstronics.ceibro.data.database.dao.TopicsV2Dao
import com.zstronics.ceibro.data.database.models.projects.CeibroGroupsV2
import com.zstronics.ceibro.data.database.models.tasks.CeibroDrawingPins
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.AllCeibroConnections
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.CeibroConnectionGroupV2
import com.zstronics.ceibro.data.repos.projects.drawing.DrawingV2
import com.zstronics.ceibro.data.repos.task.ITaskRepository
import com.zstronics.ceibro.data.repos.task.TaskRootStateTags
import com.zstronics.ceibro.data.repos.task.models.TopicsResponse
import com.zstronics.ceibro.data.repos.task.models.TopicsV2DatabaseEntity
import com.zstronics.ceibro.ui.tasks.v3.TasksParentTabV3VM
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LocationsV2VM @Inject constructor(
    override val viewState: LocationsV2State,
    private val drawingPinsDao: DrawingPinsV2Dao,
    val downloadedDrawingV2Dao: DownloadedDrawingV2Dao,
    private val groupsV2Dao: GroupsV2Dao,
    private val taskRepository: ITaskRepository,
    private val topicsV2Dao: TopicsV2Dao,
    val taskDao: TaskV2Dao,
) : HiltBaseViewModel<ILocationsV2.State>(), ILocationsV2.ViewModel {

    var _selectedTaskTypeOngoingState: MutableLiveData<String> =
        MutableLiveData(TaskRootStateTags.All.tagValue)
    var selectedTaskTypeOngoingState: LiveData<String> = _selectedTaskTypeOngoingState
    var typeToShowOngoing = "All"


    //filters
    var userConnectionAndRoleList =
        Pair(ArrayList<AllCeibroConnections.CeibroConnection>(), ArrayList<String>())
    var selectedGroups = ArrayList<CeibroConnectionGroupV2>()

    var userFilterCounter = "0"
    var tagFilterCounter = "0"


    var _applyFilter: MutableLiveData<Boolean> = MutableLiveData(false)
    var applyFilter: LiveData<Boolean> = _applyFilter

    var selectedTagsForFilter = ArrayList<TopicsResponse.TopicData>()

    var index = -10
    var oldPosition = -11
    var drawingFirstLoad = true

    var _drawingFile: MutableLiveData<DrawingV2?> =
        MutableLiveData()
    val drawingFile: LiveData<DrawingV2?> = _drawingFile

    private val _existingDrawingPins: MutableLiveData<MutableList<CeibroDrawingPins>> =
        MutableLiveData()
    val existingDrawingPins: LiveData<MutableList<CeibroDrawingPins>> = _existingDrawingPins


    private var _existingGroup: MutableLiveData<CeibroGroupsV2> =
        MutableLiveData()
    val existingGroup: LiveData<CeibroGroupsV2> = _existingGroup


    private val _filterExistingDrawingPins: MutableLiveData<MutableList<CeibroDrawingPins>> =
        MutableLiveData()
    val filterExistingDrawingPins: LiveData<MutableList<CeibroDrawingPins>> =
        _filterExistingDrawingPins

    var cameFromProject: Boolean = true


    private val _allTopics: MutableLiveData<MutableList<TopicsResponse.TopicData>?> =
        MutableLiveData()
    val allTopics: MutableLiveData<MutableList<TopicsResponse.TopicData>?> = _allTopics
    var originalAllTopics = listOf<TopicsResponse.TopicData>()

    private var _allTopicsGrouped: MutableLiveData<MutableList<TasksParentTabV3VM.CeibroTopicGroup>> =
        MutableLiveData()
    val allTopicsGrouped: MutableLiveData<MutableList<TasksParentTabV3VM.CeibroTopicGroup>> =
        _allTopicsGrouped

    private val _recentTopics: MutableLiveData<MutableList<TopicsResponse.TopicData>?> =
        MutableLiveData()
    val recentTopics: MutableLiveData<MutableList<TopicsResponse.TopicData>?> = _recentTopics
    var originalRecentTopics = listOf<TopicsResponse.TopicData>()
    var oldSelectedTags: MutableLiveData<MutableList<TopicsResponse.TopicData>> = MutableLiveData()


    fun getDrawingPins(drawingId: String) {
        launch {
            val drawingPins = drawingPinsDao.getAllDrawingPins(drawingId)
            if (drawingPins.isNotEmpty()) {
                _existingDrawingPins.postValue(drawingPins.toMutableList())
            } else {
                _existingDrawingPins.postValue(mutableListOf())
            }
        }
    }

    fun getGroupDrawingsByGroupId(groupId: String) {
        launch {

            val ceibroGroupsV2: CeibroGroupsV2? = groupsV2Dao.getGroupByGroupId(groupId)
            ceibroGroupsV2?.let {
                _existingGroup.value = it
            }

        }
    }

    fun checkFilter(filtersList: ArrayList<String>) {
        val list: ArrayList<CeibroDrawingPins> = arrayListOf()
        list.clear()
        if (filtersList.isNotEmpty()) {
            existingDrawingPins.value?.forEach {
                val taskRootState = it.taskData.taskRootState
                if (filtersList.contains(taskRootState.lowercase())) {
                    if (!list.contains(it)) {
                        list.add(it)
                    }
                }
            }
            if (selectedTaskTypeOngoingState.value.equals(TaskRootStateTags.All.tagValue, true)) {
                _filterExistingDrawingPins.value = list
            } else if (selectedTaskTypeOngoingState.value.equals(
                    TaskRootStateTags.FromMe.tagValue,
                    true
                )
            ) {
                val fromMeList: ArrayList<CeibroDrawingPins> = arrayListOf()

                list.forEach { item ->
                    if (item.taskData.rootState.equals(TaskRootStateTags.FromMe.tagValue, true)) {
                        if (!fromMeList.contains(item)) {
                            fromMeList.add(item)
                        }
                    }
                }

                _filterExistingDrawingPins.value = fromMeList
            } else if (selectedTaskTypeOngoingState.value.equals(
                    TaskRootStateTags.ToMe.tagValue,
                    true
                )
            ) {

                val toMeList: ArrayList<CeibroDrawingPins> = arrayListOf()

                list.forEach { item ->
                    if (item.taskData.rootState.equals(TaskRootStateTags.ToMe.tagValue, true)) {
                        if (!toMeList.contains(item)) {
                            toMeList.add(item)
                        }
                    }
                }
                _filterExistingDrawingPins.value = toMeList
            }

        } else {
            _filterExistingDrawingPins.value = arrayListOf()
        }
    }

    fun filterTopics(search: String) {
        if (search.isEmpty()) {
            if (originalAllTopics.isNotEmpty()) {
                _allTopics.postValue(originalAllTopics as MutableList<TopicsResponse.TopicData>?)
            }
            if (originalRecentTopics.isNotEmpty()) {
                _recentTopics.postValue(originalRecentTopics as MutableList<TopicsResponse.TopicData>?)
            }
            return
        }

        //For recent topics
        val recentFiltered = originalRecentTopics.filter {
            it.topic.lowercase().contains(search.trim(), true)
        }
        if (recentFiltered.isNotEmpty())
            _recentTopics.postValue(recentFiltered as MutableList<TopicsResponse.TopicData>?)
        else
            _recentTopics.postValue(mutableListOf())

        //For all topics
        val allFiltered = originalAllTopics.filter {
            it.topic.lowercase().contains(search.trim(), true)
        }
        if (allFiltered.isNotEmpty())
            _allTopics.postValue(allFiltered as MutableList<TopicsResponse.TopicData>?)
        else
            _allTopics.postValue(mutableListOf())
    }

    fun getAllTopics(callBack: (allTopics: TopicsResponse?) -> Unit) {
        launch {
            taskRepository.getAllTopics { isSuccess, error, allTopics ->
                if (isSuccess) {
                    launch {
                        allTopics?.let {
                            TopicsV2DatabaseEntity(
                                0,
                                topicsData = it
                            )
                        }?.let {
                            topicsV2Dao.insertTopicData(
                                it
                            )
                        }
                    }
                    val allTopic1 = allTopics?.allTopics
                    val recentTopic1 = allTopics?.recentTopics

                    if (allTopic1?.isNotEmpty() == true) {
                        originalAllTopics = allTopic1
                    }
                    if (recentTopic1?.isNotEmpty() == true) {
                        originalRecentTopics = recentTopic1
                    }

                    _allTopics.postValue(allTopic1 as MutableList<TopicsResponse.TopicData>?)
                    _recentTopics.postValue(recentTopic1 as MutableList<TopicsResponse.TopicData>?)
                    callBack.invoke(allTopics)
                } else {
                    launch {
                        val topicsData = topicsV2Dao.getTopicsData()
                        if (topicsData != null) {
                            val allTopics1 = topicsData.topicsData
                            val allTopic1 = allTopics1.allTopics
                            val recentTopic1 = allTopics1.recentTopics

                            if (allTopic1.isNotEmpty()) {
                                originalAllTopics = allTopic1
                                _allTopics.postValue(allTopic1 as MutableList<TopicsResponse.TopicData>?)
                            }
                            if (recentTopic1.isNotEmpty()) {
                                originalRecentTopics = recentTopic1
                                _recentTopics.postValue(recentTopic1 as MutableList<TopicsResponse.TopicData>?)
                            }
                            callBack.invoke(allTopics1)
                        } else {
                            callBack.invoke(null)
                        }
                    }
                }
            }
        }
    }


}