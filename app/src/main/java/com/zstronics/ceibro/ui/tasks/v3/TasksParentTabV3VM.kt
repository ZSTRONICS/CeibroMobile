package com.zstronics.ceibro.ui.tasks.v3

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.CeibroApplication
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.database.dao.DownloadedDrawingV2Dao
import com.zstronics.ceibro.data.database.dao.DrawingPinsV2Dao
import com.zstronics.ceibro.data.database.dao.GroupsV2Dao
import com.zstronics.ceibro.data.database.dao.ProjectsV2Dao
import com.zstronics.ceibro.data.database.dao.TaskV2Dao
import com.zstronics.ceibro.data.database.dao.TopicsV2Dao
import com.zstronics.ceibro.data.database.models.projects.CeibroProjectV2
import com.zstronics.ceibro.data.database.models.tasks.CeibroTaskV2
import com.zstronics.ceibro.data.repos.dashboard.IDashboardRepository
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.AllCeibroConnections
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.CeibroConnectionGroupV2
import com.zstronics.ceibro.data.repos.projects.IProjectRepository
import com.zstronics.ceibro.data.repos.task.ITaskRepository
import com.zstronics.ceibro.data.repos.task.TaskRootStateTags
import com.zstronics.ceibro.data.repos.task.models.NewTopicCreateRequest
import com.zstronics.ceibro.data.repos.task.models.TopicsResponse
import com.zstronics.ceibro.data.repos.task.models.TopicsV2DatabaseEntity
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.ui.socket.LocalEvents
import com.zstronics.ceibro.ui.tasks.task.TaskStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TasksParentTabV3VM @Inject constructor(
    override val viewState: TasksParentTabV3State,
    val sessionManager: SessionManager,
    val dashboardRepository: IDashboardRepository,
    val taskDao: TaskV2Dao,
    val groupsV2Dao: GroupsV2Dao,
    val drawingPinsDao: DrawingPinsV2Dao,
    val downloadedDrawingV2Dao: DownloadedDrawingV2Dao,
    val projectsV2Dao: ProjectsV2Dao,
    private val projectRepository: IProjectRepository,
    private val taskRepository: ITaskRepository,
    private val topicsV2Dao: TopicsV2Dao
) : HiltBaseViewModel<ITasksParentTabV3.State>(), ITasksParentTabV3.ViewModel {
    val user = sessionManager.getUser().value
    var _selectedTaskTypeOngoingState: MutableLiveData<String> =
        MutableLiveData(TaskRootStateTags.All.tagValue)
    var selectedTaskTypeOngoingState: LiveData<String> = _selectedTaskTypeOngoingState

    var _selectedTaskTypeApprovalState: MutableLiveData<String> =
        MutableLiveData(TaskRootStateTags.All.tagValue)
    var selectedTaskTypeApprovalState: LiveData<String> = _selectedTaskTypeApprovalState

    var _selectedTaskTypeClosedState: MutableLiveData<String> =
        MutableLiveData(TaskRootStateTags.All.tagValue)
    var selectedTaskTypeClosedState: LiveData<String> = _selectedTaskTypeClosedState

    var _applyFilter: MutableLiveData<Boolean> = MutableLiveData(false)
    var applyFilter: LiveData<Boolean> = _applyFilter

    var selectedProjectsForFilter = ArrayList<CeibroProjectV2>()
    var selectedTagsForFilter = ArrayList<TopicsResponse.TopicData>()

    var firstStartOfParentFragment = true
    var isFirstStartOfOngoingFragment = true
    var isFirstStartOfApprovalFragment = true
    var isFirstStartOfClosedFragment = true
    var isSearchingTasks = false


    private val _ongoingToMeTasks: MutableLiveData<MutableList<CeibroTaskV2>> = MutableLiveData()
    val ongoingToMeTasks: LiveData<MutableList<CeibroTaskV2>> = _ongoingToMeTasks
    var originalOngoingToMeTasks: MutableList<CeibroTaskV2> = mutableListOf()

    private val _ongoingFromMeTasks: MutableLiveData<MutableList<CeibroTaskV2>> = MutableLiveData()
    val ongoingFromMeTasks: LiveData<MutableList<CeibroTaskV2>> = _ongoingFromMeTasks
    var originalOngoingFromMeTasks: MutableList<CeibroTaskV2> = mutableListOf()

    private val _ongoingAllTasks: MutableLiveData<MutableList<CeibroTaskV2>> = MutableLiveData()
    val ongoingAllTasks: LiveData<MutableList<CeibroTaskV2>> = _ongoingAllTasks
    var originalOngoingAllTasks: MutableList<CeibroTaskV2> = mutableListOf()


    private val _approvalInReviewTasks: MutableLiveData<MutableList<CeibroTaskV2>> =
        MutableLiveData()
    val approvalInReviewTasks: LiveData<MutableList<CeibroTaskV2>> = _approvalInReviewTasks
    var originalApprovalInReviewTasks: MutableList<CeibroTaskV2> = mutableListOf()

    private val _approvalToReviewTasks: MutableLiveData<MutableList<CeibroTaskV2>> =
        MutableLiveData()
    val approvalToReviewTasks: LiveData<MutableList<CeibroTaskV2>> = _approvalToReviewTasks
    var originalApprovalToReviewTasks: MutableList<CeibroTaskV2> = mutableListOf()

    private val _approvalAllTasks: MutableLiveData<MutableList<CeibroTaskV2>> = MutableLiveData()
    val approvalAllTasks: LiveData<MutableList<CeibroTaskV2>> = _approvalAllTasks
    var originalApprovalAllTasks: MutableList<CeibroTaskV2> = mutableListOf()


    private val _closedToMeTasks: MutableLiveData<MutableList<CeibroTaskV2>> = MutableLiveData()
    val closedToMeTasks: LiveData<MutableList<CeibroTaskV2>> = _closedToMeTasks
    var originalClosedToMeTasks: MutableList<CeibroTaskV2> = mutableListOf()

    private val _closedFromMeTasks: MutableLiveData<MutableList<CeibroTaskV2>> = MutableLiveData()
    val closedFromMeTasks: LiveData<MutableList<CeibroTaskV2>> = _closedFromMeTasks
    var originalClosedFromMeTasks: MutableList<CeibroTaskV2> = mutableListOf()

    private val _closedAllTasks: MutableLiveData<MutableList<CeibroTaskV2>> = MutableLiveData()
    val closedAllTasks: LiveData<MutableList<CeibroTaskV2>> = _closedAllTasks
    var originalClosedAllTasks: MutableList<CeibroTaskV2> = mutableListOf()


    private val _allProjects: MutableLiveData<MutableList<CeibroProjectV2>> =
        MutableLiveData()
    val allProjects: LiveData<MutableList<CeibroProjectV2>> = _allProjects
    var originalAllProjects: MutableList<CeibroProjectV2> = mutableListOf()


    private val _allFavoriteProjects: MutableLiveData<MutableList<CeibroProjectV2>> =
        MutableLiveData()
    val allFavoriteProjects: LiveData<MutableList<CeibroProjectV2>> = _allFavoriteProjects
    var originalFavoriteProjects: MutableList<CeibroProjectV2> = mutableListOf()

    private val _allRecentProjects: MutableLiveData<MutableList<CeibroProjectV2>> =
        MutableLiveData()
    val allRecentProjects: LiveData<MutableList<CeibroProjectV2>> = _allRecentProjects
    var originalRecentProjects: MutableList<CeibroProjectV2> = mutableListOf()


    val projects = sessionManager.getProjects().value
    var taskFilters: LocalEvents.ApplyFilterOnTask? = null
    var subTaskFilters: LocalEvents.ApplyFilterOnSubTask? = null

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
    var oldSelectedTags: MutableLiveData<MutableList<TopicsResponse.TopicData>> = MutableLiveData()

    init {
        if (sessionManager.getUser().value?.id.isNullOrEmpty()) {
            sessionManager.setUser()
            sessionManager.setToken()
        }
    }


    //filters
    var selectedConnections = ArrayList<AllCeibroConnections.CeibroConnection>()
    var selectedGroups = ArrayList<CeibroConnectionGroupV2>()

    override fun onFirsTimeUiCreate(bundle: Bundle?) {
        super.onFirsTimeUiCreate(bundle)
        launch {
            loadAllTasks() {

            }
        }

        getAllProjects()
        getFavoriteProjects()

    }


    fun loadAllTasks(callBack: () -> Unit) {
        launch {
            val rootOngoingAllTasks =
                CeibroApplication.CookiesManager.rootOngoingAllTasks.value ?: mutableListOf()
            val rootOngoingToMeTasks =
                CeibroApplication.CookiesManager.rootOngoingToMeTasks.value ?: mutableListOf()
            val rootOngoingFromMeTasks =
                CeibroApplication.CookiesManager.rootOngoingFromMeTasks.value ?: mutableListOf()

            if (rootOngoingAllTasks.isNotEmpty()) {

                if (isFirstStartOfOngoingFragment) {
                    _selectedTaskTypeOngoingState.value = TaskRootStateTags.All.tagValue
                    isFirstStartOfOngoingFragment = false
                }

                _ongoingAllTasks.postValue(rootOngoingAllTasks)
                _ongoingToMeTasks.postValue(rootOngoingToMeTasks)
                _ongoingFromMeTasks.postValue(rootOngoingFromMeTasks)

                originalOngoingAllTasks = rootOngoingAllTasks
                originalOngoingToMeTasks = rootOngoingToMeTasks
                originalOngoingFromMeTasks = rootOngoingFromMeTasks

            } else {

                val rootOngoingAllTasksDB =
                    taskDao.getRootAllTasks(TaskRootStateTags.Ongoing.tagValue)
                        .toMutableList()

                val rootOngoingToMeTasksDB =
                    rootOngoingAllTasksDB.filter {
                        it.taskRootState.equals(TaskRootStateTags.Ongoing.tagValue, true) &&
                                (it.toMeState.equals(
                                    TaskStatus.NEW.name,
                                    true
                                ) || it.toMeState.equals(TaskStatus.ONGOING.name, true))
                    }
                        .sortedByDescending { it.updatedAt }.toMutableList()

                val rootOngoingFromMeTasksDB =
                    rootOngoingAllTasksDB.filter {
                        it.taskRootState.equals(TaskRootStateTags.Ongoing.tagValue, true) &&
                                (it.fromMeState.equals(
                                    TaskStatus.UNREAD.name,
                                    true
                                ) || it.fromMeState.equals(TaskStatus.ONGOING.name, true))
                    }
                        .sortedByDescending { it.updatedAt }.toMutableList()


                CeibroApplication.CookiesManager.rootOngoingAllTasks.postValue(rootOngoingAllTasksDB)
                CeibroApplication.CookiesManager.rootOngoingToMeTasks.postValue(
                    rootOngoingToMeTasksDB
                )
                CeibroApplication.CookiesManager.rootOngoingFromMeTasks.postValue(
                    rootOngoingFromMeTasksDB
                )

                if (isFirstStartOfOngoingFragment) {
                    _selectedTaskTypeOngoingState.value = TaskRootStateTags.All.tagValue
                    isFirstStartOfOngoingFragment = false
                }

                _ongoingAllTasks.postValue(rootOngoingAllTasks)
                _ongoingToMeTasks.postValue(rootOngoingToMeTasks)
                _ongoingFromMeTasks.postValue(rootOngoingFromMeTasks)

                originalOngoingAllTasks = rootOngoingAllTasks
                originalOngoingToMeTasks = rootOngoingToMeTasks
                originalOngoingFromMeTasks = rootOngoingFromMeTasks

            }


            val rootApprovalAllTasks =
                CeibroApplication.CookiesManager.rootApprovalAllTasks.value ?: mutableListOf()
            val rootApprovalInReviewPendingTasks =
                CeibroApplication.CookiesManager.rootApprovalInReviewPendingTasks.value
                    ?: mutableListOf()
            val rootApprovalToReviewTasks =
                CeibroApplication.CookiesManager.rootApprovalToReviewTasks.value ?: mutableListOf()

            if (rootApprovalAllTasks.isNotEmpty()) {

                _approvalAllTasks.postValue(rootApprovalAllTasks)
                _approvalInReviewTasks.postValue(rootApprovalInReviewPendingTasks)
                _approvalToReviewTasks.postValue(rootApprovalToReviewTasks)

                originalApprovalAllTasks = rootApprovalAllTasks
                originalApprovalInReviewTasks = rootApprovalInReviewPendingTasks
                originalApprovalToReviewTasks = rootApprovalToReviewTasks

                if (isFirstStartOfApprovalFragment) {
                    _selectedTaskTypeApprovalState.value = TaskRootStateTags.All.tagValue
                    isFirstStartOfApprovalFragment = false
                }

            } else {

                val rootApprovalAllTasksDB =
                    taskDao.getRootAllTasks(TaskRootStateTags.Approval.tagValue)
                        .toMutableList()

                val rootApprovalInReviewPendingTasksDB =
                    rootApprovalAllTasksDB.filter {
                        it.taskRootState.equals(TaskRootStateTags.Approval.tagValue, true) &&
                                (it.userSubState.equals(TaskRootStateTags.InReview.tagValue, true))
                    }
                        .sortedByDescending { it.updatedAt }.toMutableList()

                val rootApprovalToReviewTasksDB =
                    rootApprovalAllTasksDB.filter {
                        it.taskRootState.equals(TaskRootStateTags.Approval.tagValue, true) &&
                                (it.userSubState.equals(TaskRootStateTags.ToReview.tagValue, true))
                    }
                        .sortedByDescending { it.updatedAt }.toMutableList()

                CeibroApplication.CookiesManager.rootApprovalAllTasks.postValue(
                    rootApprovalAllTasksDB
                )
                CeibroApplication.CookiesManager.rootApprovalInReviewPendingTasks.postValue(
                    rootApprovalInReviewPendingTasksDB
                )
                CeibroApplication.CookiesManager.rootApprovalToReviewTasks.postValue(
                    rootApprovalToReviewTasksDB
                )

                _approvalAllTasks.postValue(rootApprovalAllTasksDB)
                _approvalInReviewTasks.postValue(rootApprovalInReviewPendingTasksDB)
                _approvalToReviewTasks.postValue(rootApprovalToReviewTasksDB)

                originalApprovalAllTasks = rootApprovalAllTasks
                originalApprovalInReviewTasks = rootApprovalInReviewPendingTasksDB
                originalApprovalToReviewTasks = rootApprovalToReviewTasksDB

                if (isFirstStartOfApprovalFragment) {
                    _selectedTaskTypeApprovalState.value = TaskRootStateTags.All.tagValue
                    isFirstStartOfApprovalFragment = false
                }

            }


            val rootClosedAllTasks =
                CeibroApplication.CookiesManager.rootClosedAllTasks.value ?: mutableListOf()
            val rootClosedToMeTasks =
                CeibroApplication.CookiesManager.rootClosedToMeTasks.value ?: mutableListOf()
            val rootClosedFromMeTasks =
                CeibroApplication.CookiesManager.rootClosedFromMeTasks.value ?: mutableListOf()

            if (rootClosedAllTasks.isNotEmpty()) {

                _closedAllTasks.postValue(rootClosedAllTasks)
                _closedToMeTasks.postValue(rootClosedToMeTasks)
                _closedFromMeTasks.postValue(rootClosedFromMeTasks)

                originalClosedAllTasks = rootClosedAllTasks
                originalClosedToMeTasks = rootClosedToMeTasks
                originalClosedFromMeTasks = rootClosedFromMeTasks

                if (isFirstStartOfClosedFragment) {
                    _selectedTaskTypeClosedState.value = TaskRootStateTags.All.tagValue
                    isFirstStartOfClosedFragment = false
                }

            } else {

                val rootClosedAllTasksDB =
                    taskDao.getRootAllTasks(TaskRootStateTags.Closed.tagValue)
                        .toMutableList()

                val rootClosedToMeTasksDB =
                    rootClosedAllTasksDB.filter {
                        it.taskRootState.equals(TaskRootStateTags.Closed.tagValue, true) &&
                                (it.toMeState.equals(TaskStatus.DONE.name, true))
                    }
                        .sortedByDescending { it.updatedAt }.toMutableList()

                val rootClosedFromMeTasksDB =
                    rootClosedAllTasksDB.filter {
                        it.taskRootState.equals(TaskRootStateTags.Closed.tagValue, true) &&
                                (it.fromMeState.equals(TaskStatus.DONE.name, true))
                    }
                        .sortedByDescending { it.updatedAt }.toMutableList()

                CeibroApplication.CookiesManager.rootClosedAllTasks.postValue(rootClosedAllTasksDB)
                CeibroApplication.CookiesManager.rootClosedToMeTasks.postValue(rootClosedToMeTasksDB)
                CeibroApplication.CookiesManager.rootClosedFromMeTasks.postValue(
                    rootClosedFromMeTasksDB
                )

                _closedAllTasks.postValue(rootClosedAllTasksDB)
                _closedToMeTasks.postValue(rootClosedToMeTasksDB)
                _closedFromMeTasks.postValue(rootClosedFromMeTasksDB)

                originalClosedAllTasks = rootClosedAllTasksDB
                originalClosedToMeTasks = rootClosedToMeTasksDB
                originalClosedFromMeTasks = rootClosedFromMeTasksDB

                if (isFirstStartOfClosedFragment) {
                    _selectedTaskTypeClosedState.value = TaskRootStateTags.All.tagValue
                    isFirstStartOfClosedFragment = false
                }

            }

        }

    }


    override fun getProjectName(context: Context) {

    }

    override fun getAllProjects() {
        launch {
            val allProjects = projectsV2Dao.getAllProjectsNotFavorite()
            if (allProjects?.isNotEmpty() == true) {
                _allProjects.postValue(allProjects.toMutableList())
                originalAllProjects = allProjects.toMutableList()
            } else {
                _allProjects.postValue(mutableListOf())
                originalAllProjects = mutableListOf()
            }
        }
    }

    override fun getFavoriteProjects() {
        launch {
            val allFavoriteProjects = projectsV2Dao.getAllFavoriteProjects()
            if (allFavoriteProjects.isNotEmpty()) {
                _allFavoriteProjects.postValue(allFavoriteProjects.toMutableList())
                originalFavoriteProjects = allFavoriteProjects.toMutableList()
            } else {
                _allFavoriteProjects.postValue(mutableListOf())
                originalFavoriteProjects = mutableListOf()
            }
        }
    }

    override fun getRecentProjects() {
        launch {
            val allRecentProjects = projectsV2Dao.getAllRecentUsedProjects()
            if (allRecentProjects.isNotEmpty()) {
                _allRecentProjects.postValue(allRecentProjects.toMutableList())
                originalRecentProjects = allRecentProjects.toMutableList()
            } else {
                _allRecentProjects.postValue(mutableListOf())
                originalRecentProjects = mutableListOf()
            }
        }
    }


    override fun hideProject(
        hidden: Boolean,
        projectId: String,
        callBack: (isSuccess: Boolean) -> Unit
    ) {

        launch {
            loading(true)
            when (val response = projectRepository.updateHideProjectStatus(
                hidden = hidden,
                projectId = projectId
            )) {
                is ApiResponse.Success -> {
                    val ceibroProjectV2 = response.data.updatedProject
                    updateProjectInLocal(ceibroProjectV2, projectsV2Dao)
                    loading(false, "")
                    callBack(true)
                }

                is ApiResponse.Error -> {
                    loading(false, response.error.message)
                    callBack(false)
                }
            }
        }
    }

    override fun updateFavoriteProjectStatus(
        favorite: Boolean,
        projectId: String,
        callBack: (isSuccess: Boolean) -> Unit
    ) {

        launch {
            loading(true)
            when (val response = projectRepository.updateFavoriteProjectStatus(
                favorite = favorite,
                projectId = projectId
            )) {
                is ApiResponse.Success -> {
                    val ceibroProjectV2 = response.data.updatedProject
                    updateProjectInLocal(ceibroProjectV2, projectsV2Dao)
                    loading(false, "")
                    callBack(true)
                }

                is ApiResponse.Error -> {
                    loading(false, response.error.message)
                    callBack(false)
                }
            }
        }
    }

    fun reloadData() {
        getAllProjects()
        getFavoriteProjects()
//        getRecentProjects()
    }


    fun filterAllProjects(search: String) {
        if (search.isEmpty()) {
            if (originalAllProjects.isNotEmpty()) {
                _allProjects.postValue(originalAllProjects as MutableList<CeibroProjectV2>)
            }
            return
        }
        val filtered = originalAllProjects.filter {
            (it.title.isNotEmpty() && it.title.lowercase().contains(search, true)) ||
                    (("${it.creator.firstName} ${it.creator.surName}").isNotEmpty() && ("${it.creator.firstName} ${it.creator.surName}").lowercase()
                        .contains(search, true)) ||
                    (!it.creator.companyName.isNullOrEmpty() && it.creator.companyName.lowercase()
                        .contains(search, true))
        }
        if (filtered.isNotEmpty())
            _allProjects.postValue(filtered as MutableList<CeibroProjectV2>)
        else
            _allProjects.postValue(mutableListOf())
    }


    fun filterFavoriteProjects(search: String) {
        if (search.isEmpty()) {
            if (originalFavoriteProjects.isNotEmpty()) {
                _allFavoriteProjects.postValue(originalFavoriteProjects as MutableList<CeibroProjectV2>)
            }
            return
        }
        val filtered = originalFavoriteProjects.filter {
            (it.title.isNotEmpty() && it.title.lowercase().contains(search, true)) ||
                    (("${it.creator.firstName} ${it.creator.surName}").isNotEmpty() && ("${it.creator.firstName} ${it.creator.surName}").lowercase()
                        .contains(search, true)) ||
                    (!it.creator.companyName.isNullOrEmpty() && it.creator.companyName.lowercase()
                        .contains(search, true))
        }
        if (filtered.isNotEmpty())
            _allFavoriteProjects.postValue(filtered as MutableList<CeibroProjectV2>)
        else
            _allFavoriteProjects.postValue(mutableListOf())
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

    fun saveTopic(
        topic: String,
        callBack: (isSuccess: Boolean, newTopic: TopicsResponse.TopicData?) -> Unit
    ) {
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

    fun sortList(list: List<CeibroTaskV2>): MutableList<CeibroTaskV2> {

        if (isFilterListEmpty()) {
            return list.toMutableList()
        }

        val filteredTasks =
            list.filter { task ->
                (task.tags?.any { tag -> (selectedTagsForFilter.any { it.topic == tag.topic }) || selectedTagsForFilter.size == 0 } == true) &&
                        ((selectedProjectsForFilter.any { project ->
                            project._id == task.project?.id
                        }) || selectedProjectsForFilter.size == 0)
            }.toMutableList()


        return filteredTasks
    }

    private fun isFilterListEmpty(): Boolean {
        return selectedTagsForFilter.size == 0 && selectedProjectsForFilter.size == 0
    }
}