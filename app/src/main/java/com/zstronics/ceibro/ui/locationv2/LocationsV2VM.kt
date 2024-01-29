package com.zstronics.ceibro.ui.locationv2

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.database.dao.DownloadedDrawingV2Dao
import com.zstronics.ceibro.data.database.dao.DrawingPinsV2Dao
import com.zstronics.ceibro.data.database.dao.GroupsV2Dao
import com.zstronics.ceibro.data.database.models.projects.CeibroGroupsV2
import com.zstronics.ceibro.data.database.models.tasks.CeibroDrawingPins
import com.zstronics.ceibro.data.repos.projects.drawing.DrawingV2
import com.zstronics.ceibro.data.repos.task.TaskRootStateTags
import com.zstronics.ceibro.ui.tasks.task.TaskStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LocationsV2VM @Inject constructor(
    override val viewState: LocationsV2State,
    private val drawingPinsDao: DrawingPinsV2Dao,
    val downloadedDrawingV2Dao: DownloadedDrawingV2Dao,
    private val groupsV2Dao: GroupsV2Dao,
) : HiltBaseViewModel<ILocationsV2.State>(), ILocationsV2.ViewModel {

    var index = -10
    var oldPosition = -11

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

    fun checkFilter(filterList: ArrayList<Pair<String, String>>) {

        val list: ArrayList<CeibroDrawingPins> = arrayListOf()

        if (filterList.isEmpty() || existingDrawingPins.value?.isEmpty() == true) {
            _filterExistingDrawingPins.value = mutableListOf()
            return
        }

        existingDrawingPins.value?.forEach {
            val rootState = it.taskData.rootState
            val hiddenByMe = it.taskData.isHiddenByMe
            val creatorState = it.taskData.creatorState.lowercase()
            val userSubState = it.taskData.userSubState.lowercase()
            val taskRootState = it.taskData.taskRootState
            val toMeState = it.taskData.toMeState.lowercase()
            val fromMeState = it.taskData.fromMeState.lowercase()
            val hiddenState = it.taskData.hiddenState.lowercase()

            when {


                rootState.equals(TaskRootStateTags.FromMe.tagValue, true) -> {
                    if (hiddenByMe) {
                        if (hiddenState.equals(TaskStatus.ONGOING.name, true)) {
                            val pair =
                                Pair(TaskRootStateTags.Hidden.tagValue.lowercase(), hiddenState)
                            if (filterList.contains(pair)) {
                                list.add(it)
                            }
                        } else if (hiddenState.equals(
                                TaskStatus.DONE.name,
                                true
                            )
                        ) {
                            val pair =
                                Pair(TaskRootStateTags.Hidden.tagValue.lowercase(), hiddenState)
                            if (filterList.contains(pair)) {
                                list.add(it)
                            }
                        }
                    } else if (creatorState.equals(
                            TaskStatus.CANCELED.name,
                            true
                        )
                    ) {
                        val pair = Pair(TaskRootStateTags.Hidden.tagValue.lowercase(), creatorState)
                        if (filterList.contains(pair)) {
                            list.add(it)
                        }
                    } else {
                        if (fromMeState.equals(TaskStatus.UNREAD.name, true)) {
                            val pair =
                                Pair(TaskRootStateTags.FromMe.tagValue.lowercase(), fromMeState)
                            if (filterList.contains(pair)) {
                                list.add(it)
                            }
                        } else if (fromMeState.equals(
                                TaskStatus.ONGOING.name,
                                true
                            )
                        ) {
                            val pair =
                                Pair(TaskRootStateTags.FromMe.tagValue.lowercase(), fromMeState)
                            if (filterList.contains(pair)) {
                                list.add(it)
                            }
                        } else if (fromMeState.equals(
                                TaskStatus.DONE.name,
                                true
                            )
                        ) {
                            val pair =
                                Pair(TaskRootStateTags.FromMe.tagValue.lowercase(), fromMeState)
                            if (filterList.contains(pair)) {
                                list.add(it)
                            }
                        }
                    }
                }


                rootState.equals(TaskRootStateTags.ToMe.tagValue, true) -> {
                    if (hiddenByMe) {
                        if (hiddenState.equals(TaskStatus.ONGOING.name, true)) {
                            val pair =
                                Pair(TaskRootStateTags.Hidden.tagValue.lowercase(), hiddenState)
                            if (filterList.contains(pair)) {
                                list.add(it)
                            }
                        } else if (hiddenState.equals(
                                TaskStatus.DONE.name,
                                true
                            )
                        ) {
                            val pair =
                                Pair(TaskRootStateTags.Hidden.tagValue.lowercase(), hiddenState)
                            if (filterList.contains(pair)) {
                                list.add(it)
                            }
                        }
                    } else if (userSubState.equals(
                            TaskStatus.CANCELED.name,
                            true
                        )
                    ) {
                        val pair = Pair(TaskRootStateTags.Hidden.tagValue.lowercase(), userSubState)
                        if (filterList.contains(pair)) {
                            list.add(it)
                        }
                    } else {
                        if (toMeState.equals(TaskStatus.NEW.name, true)) {
                            val pair = Pair(TaskRootStateTags.ToMe.tagValue.lowercase(), toMeState)
                            if (filterList.contains(pair)) {
                                list.add(it)
                            }
                        } else if (toMeState.equals(
                                TaskStatus.ONGOING.name,
                                true
                            )
                        ) {
                            val pair = Pair(TaskRootStateTags.ToMe.tagValue.lowercase(), toMeState)
                            if (filterList.contains(pair)) {
                                list.add(it)
                            }
                        } else if (toMeState.equals(
                                TaskStatus.DONE.name,
                                true
                            )
                        ) {
                            val pair = Pair(TaskRootStateTags.ToMe.tagValue.lowercase(), toMeState)
                            if (filterList.contains(pair)) {
                                list.add(it)
                            }
                        }
                    }
                }


                /*rootState.equals(TaskRootStateTags.Hidden.tagValue, true) -> {
                    val pair = Pair(rootState, hiddenState)
                    if (filterList.contains(pair)) {
                        list.add(it)
                    }
                }*/
            }
        }

        _filterExistingDrawingPins.value = list
    }

}