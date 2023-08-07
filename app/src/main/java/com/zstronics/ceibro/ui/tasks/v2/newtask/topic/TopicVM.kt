package com.zstronics.ceibro.ui.tasks.v2.newtask.topic

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.database.dao.TopicsV2Dao
import com.zstronics.ceibro.data.repos.task.ITaskRepository
import com.zstronics.ceibro.data.repos.task.models.NewTopicCreateRequest
import com.zstronics.ceibro.data.repos.task.models.TopicsResponse
import com.zstronics.ceibro.data.sessions.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TopicVM @Inject constructor(
    override val viewState: TopicState,
    private val taskRepository: ITaskRepository,
    sessionManager: SessionManager,
    private val topicsV2Dao: TopicsV2Dao
) : HiltBaseViewModel<ITopic.State>(), ITopic.ViewModel {
    val user = sessionManager.getUser().value

    private val _allTopics: MutableLiveData<MutableList<TopicsResponse.TopicData>?> =
        MutableLiveData()
    val allTopics: MutableLiveData<MutableList<TopicsResponse.TopicData>?> = _allTopics
    var originalAllTopics = listOf<TopicsResponse.TopicData>()

    private var _allTopicsGrouped: MutableLiveData<MutableList<CeibroTopicGroup>> =
        MutableLiveData()
    val allTopicsGrouped: MutableLiveData<MutableList<CeibroTopicGroup>> =
        _allTopicsGrouped

    private val _recentTopics: MutableLiveData<MutableList<TopicsResponse.TopicData>?> =
        MutableLiveData()
    val recentTopics: MutableLiveData<MutableList<TopicsResponse.TopicData>?> = _recentTopics
    var originalRecentTopics = listOf<TopicsResponse.TopicData>()


    fun getAllTopics(callBack: () -> Unit) {
        launch {
//            val topicsData = topicsV2Dao.getTopicsData()
//            if (topicsData != null) {
//                val allTopics = topicsData.topicsData
//                val allTopic1 = allTopics.allTopics
//                val recentTopic1 = allTopics.recentTopics
//
//                if (allTopic1.isNotEmpty()) {
//                    originalAllTopics = allTopic1
//                    _allTopics.postValue(allTopic1 as MutableList<TopicsResponse.TopicData>?)
//                }
//                if (recentTopic1.isNotEmpty()) {
//                    originalRecentTopics = recentTopic1
//                    _recentTopics.postValue(recentTopic1 as MutableList<TopicsResponse.TopicData>?)
//                }
//                callBack.invoke()
//            } else {
            taskRepository.getAllTopics { isSuccess, error, allTopics ->
                if (isSuccess) {
                    val allTopic1 = allTopics?.allTopics
                    val recentTopic1 = allTopics?.recentTopics

                    if (allTopic1?.isNotEmpty() == true) {
                        originalAllTopics = allTopic1
                        _allTopics.postValue(allTopic1 as MutableList<TopicsResponse.TopicData>?)
                    }
                    if (recentTopic1?.isNotEmpty() == true) {
                            originalRecentTopics = recentTopic1
                            _recentTopics.postValue(recentTopic1 as MutableList<TopicsResponse.TopicData>?)
                        }
                        callBack.invoke()
                    } else {
                        callBack.invoke()
                        alert(error)
                    }
                }
//            }
        }
    }

    fun saveTopic(topic: String, callBack: (isSuccess: Boolean, newTopic: TopicsResponse.TopicData?) -> Unit) {
        val request = NewTopicCreateRequest(
            topic = topic
        )
        launch {
            loading(true)
            taskRepository.saveTopic(request) { isSuccess, error, newTopicResponse ->
                if (isSuccess) {
                    val newTopic = newTopicResponse?.newTopic

                    if (newTopic != null) {
                        if (originalAllTopics.isNotEmpty()) {
                            val allTopics =
                                originalAllTopics as MutableList<TopicsResponse.TopicData>
                            allTopics.add(newTopic)
                            originalAllTopics = allTopics
                            _allTopics.postValue(allTopics)
                        } else {
                            val allTopics: MutableList<TopicsResponse.TopicData> = mutableListOf()
                            allTopics.add(newTopic)
                            originalAllTopics = allTopics
                            _allTopics.postValue(allTopics)
                        }
                    }
                    loading(false, "")
                    callBack.invoke(isSuccess, newTopic)
                } else {
                    loading(false, error)
                }
            }
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

    fun groupDataByFirstLetter(data: List<TopicsResponse.TopicData>) {
        val sections = mutableListOf<CeibroTopicGroup>()

        val groupedData = data.groupBy {
            if (it.topic.firstOrNull()?.isLetter() == true) {
                it.topic.first().lowercase()
            } else {
                '#'.toString()
            }
        }.toSortedMap(
            compareBy<String> { it != "#" }
                .then(compareBy { it.lowercase() })
                .then(compareByDescending { it == "#" })
        )

        for (mapKey in groupedData.keys) {
            sections.add(
                CeibroTopicGroup(
                    mapKey.toString().uppercase()[0],
                    groupedData[mapKey]?.sortedBy { it.topic.lowercase() }
                        ?: emptyList()
                )
            )
        }
        _allTopicsGrouped.value = sections
    }

    data class CeibroTopicGroup(
        val sectionLetter: Char,
        val items: List<TopicsResponse.TopicData>
    )
}