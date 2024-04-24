package com.zstronics.ceibro.ui.tasks.v3.canceled

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.CeibroApplication
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.toCamelCase
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
import com.zstronics.ceibro.data.database.models.tasks.TaskMemberDetail
import com.zstronics.ceibro.data.remote.TaskRemoteDataSource
import com.zstronics.ceibro.data.repos.dashboard.IDashboardRepository
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.AllCeibroConnections
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.CeibroConnectionGroupV2
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.GroupContact
import com.zstronics.ceibro.data.repos.dashboard.contacts.SyncDBContactsList
import com.zstronics.ceibro.data.repos.projects.IProjectRepository
import com.zstronics.ceibro.data.repos.task.ITaskRepository
import com.zstronics.ceibro.data.repos.task.TaskRootStateTags
import com.zstronics.ceibro.data.repos.task.models.NewTopicCreateRequest
import com.zstronics.ceibro.data.repos.task.models.TopicsResponse
import com.zstronics.ceibro.data.repos.task.models.TopicsV2DatabaseEntity
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.ui.contacts.toLightDBContacts
import com.zstronics.ceibro.ui.contacts.toLightDBGroupContacts
import com.zstronics.ceibro.ui.contacts.toLightGroupContactsFromTaskMember
import com.zstronics.ceibro.ui.socket.LocalEvents
import com.zstronics.ceibro.ui.tasks.task.TaskStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class TasksCanceledV3VM @Inject constructor(
    override val viewState: TasksCanceledV3State,
    val sessionManager: SessionManager,
    val dashboardRepository: IDashboardRepository,
    val taskDao: TaskV2Dao,
    val groupsV2Dao: GroupsV2Dao,
    val drawingPinsDao: DrawingPinsV2Dao,
    val downloadedDrawingV2Dao: DownloadedDrawingV2Dao,
    val projectsV2Dao: ProjectsV2Dao,
    private val projectRepository: IProjectRepository,
    private val taskRepository: ITaskRepository,
    private val remoteTask: TaskRemoteDataSource,
    private val topicsV2Dao: TopicsV2Dao
) : HiltBaseViewModel<ITasksCanceledV3.State>(), ITasksCanceledV3.ViewModel {

    var typeToShowCanceled = "All"

    var userFilterCounter = "0"
    var tagFilterCounter = "0"
    var projectFilterCounter = "0"

    val user = sessionManager.getUser().value

    var _applyFilter: MutableLiveData<Boolean> = MutableLiveData(false)
    var applyFilter: LiveData<Boolean> = _applyFilter

    var selectedProjectsForFilter = ArrayList<CeibroProjectV2>()
    var selectedTagsForFilter = ArrayList<TopicsResponse.TopicData>()

    var firstStartOfParentFragment = true
    var isFirstStartOfCanceledFragment = true
    var isSearchingTasks = false
    var isFirstTimeUICreated=true


    private val _canceledToMeTasks: MutableLiveData<MutableList<CeibroTaskV2>> = MutableLiveData()
    val canceledToMeTasks: LiveData<MutableList<CeibroTaskV2>> = _canceledToMeTasks
    var originalCanceledToMeTasks: MutableList<CeibroTaskV2> = mutableListOf()

    private val _canceledFromMeTasks: MutableLiveData<MutableList<CeibroTaskV2>> = MutableLiveData()
    val canceledFromMeTasks: LiveData<MutableList<CeibroTaskV2>> = _canceledFromMeTasks
    var originalCanceledFromMeTasks: MutableList<CeibroTaskV2> = mutableListOf()

    private val _canceledAllTasks: MutableLiveData<MutableList<CeibroTaskV2>> = MutableLiveData()
    val canceledAllTasks: LiveData<MutableList<CeibroTaskV2>> = _canceledAllTasks
    var originalCanceledAllTasks: MutableList<CeibroTaskV2> = mutableListOf()

    var filteredCanceledTasks: MutableList<CeibroTaskV2> = mutableListOf()


    private val _setFilteredDataToCanceledAdapter: MutableLiveData<MutableList<CeibroTaskV2>> =
        MutableLiveData()
    val setFilteredDataToCanceledAdapter: LiveData<MutableList<CeibroTaskV2>> =
        _setFilteredDataToCanceledAdapter



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


    var searchedText: String = ""
    var lastSortingType: MutableLiveData<String> = MutableLiveData("SortByActivity")

    var _selectedTaskTypeCanceledState: MutableLiveData<String> =
        MutableLiveData(TaskRootStateTags.All.tagValue)
    var selectedTaskTypeCanceledState: LiveData<String> = _selectedTaskTypeCanceledState



    init {

        if (sessionManager.getUser().value?.id.isNullOrEmpty()) {
            sessionManager.setUser()
            sessionManager.setToken()
        }
    }


    //filters
    var userConnectionAndRoleList =
        Pair(ArrayList<AllCeibroConnections.CeibroConnection>(), ArrayList<String>())

    var selectedGroups = ArrayList<CeibroConnectionGroupV2>()

    override fun onFirsTimeUiCreate(bundle: Bundle?) {
        super.onFirsTimeUiCreate(bundle)

        reloadData()
    }


    fun loadAllTasks(callBack: () -> Unit) {
        launch {
            val rootAllCanceledTasksDB =
                taskDao.getRootAllTasks(TaskRootStateTags.Canceled.tagValue.toCamelCase()).toMutableList()

            val rootCanceledToMeTasksDB =
                rootAllCanceledTasksDB.filter {
                    it.taskRootState.equals(TaskRootStateTags.Canceled.tagValue, true) &&
                            it.isAssignedToMe
                }.sortedByDescending { it.updatedAt }.toMutableList()

            val rootCanceledFromMeTasksDB =
                rootAllCanceledTasksDB.filter {
                    it.taskRootState.equals(TaskRootStateTags.Canceled.tagValue, true) &&
                            it.isCreator
                }.sortedByDescending { it.updatedAt }.toMutableList()


            filteredCanceledTasks = rootAllCanceledTasksDB
            _canceledAllTasks.postValue(rootAllCanceledTasksDB)
            originalCanceledAllTasks = rootAllCanceledTasksDB

            _canceledToMeTasks.postValue(rootCanceledToMeTasksDB)
            originalCanceledToMeTasks = rootCanceledToMeTasksDB

            _canceledFromMeTasks.postValue(rootCanceledFromMeTasksDB)
            originalCanceledFromMeTasks = rootCanceledFromMeTasksDB

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
            val sortedList = sortUsersList(list)
            return sortedList.toMutableList()
        }

        var filteredTasks =
            list.filter { task ->
                (selectedTagsForFilter.size == 0 || selectedTagsForFilter.any { tag ->
                    task.tags?.any {
                        it.topic.equals(
                            tag.topic,
                            true
                        )
                    } ?: true
                }) && (selectedProjectsForFilter.size == 0 || selectedProjectsForFilter.any { project ->
                    project.title.equals(
                        task.project?.title,
                        true
                    )
                })
            }.toMutableList()



        filteredTasks = sortUsersList(filteredTasks)
        return filteredTasks
    }


    private fun sortUsersList(list: List<CeibroTaskV2>): MutableList<CeibroTaskV2> {
        val sortedList = ArrayList<CeibroTaskV2>()
        sortedList.clear()

        val roles = userConnectionAndRoleList.second
        val connection = userConnectionAndRoleList.first
        val groupConnections: MutableList<TaskMemberDetail> = mutableListOf()

        if (selectedGroups.isNotEmpty() && roles.isEmpty()) {
            if (!roles.contains("Confirmer")) {
                roles.add("Confirmer")
            }
            if (!roles.contains("Assignee")) {
                roles.add("Assignee")
            }
            if (!roles.contains("Viewer")) {
                roles.add("Viewer")
            }
            if (!roles.contains("Creator")) {
                roles.add("Creator")
            }
        }

        selectedGroups.forEach { group ->
            groupConnections.addAll(group.contacts)
        }

        val lightConnectionContacts = connection.toLightDBContacts()
        val lightGroupContacts = groupConnections.toLightGroupContactsFromTaskMember()


        val combinedList: MutableList<SyncDBContactsList.CeibroDBContactsLight> = mutableListOf()
        combinedList.addAll(lightConnectionContacts)
        combinedList.addAll(lightGroupContacts)
        val distinctList = combinedList.distinctBy { it.phoneNumber }


        if (distinctList.isEmpty()) {
            return list.toMutableList()
        }


        list.forEach { task ->
            roles.forEach { role ->
                when (role) {
                    "Confirmer" -> {
                        if (distinctList.any { it.phoneNumber == task.confirmer?.phoneNumber }) {
                            if (!sortedList.contains(task)) {
                                sortedList.add(task)
                            }
                        }
                    }

                    "Assignee" -> {
                        if (task.assignedToState.any { assignee -> distinctList.any { con -> con.phoneNumber == assignee.phoneNumber } }) {
                            if (!sortedList.contains(task)) {
                                sortedList.add(task)
                            }
                        }
                        if (task.invitedNumbers.any { invitee -> distinctList.any { con -> con.phoneNumber == invitee.phoneNumber } }) {
                            if (!sortedList.contains(task)) {
                                sortedList.add(task)
                            }
                        }
                    }

                    "Viewer" -> {
                        task.viewer?.let { viewerList ->
                            if (viewerList.any { viewer -> distinctList.any { con -> con.phoneNumber == viewer.phoneNumber } }) {
                                if (!sortedList.contains(task)) {
                                    sortedList.add(task)
                                }
                            }
                        }

                    }

                    "Creator" -> {
                        task.creator.let { creator ->
                            if (distinctList.any { con -> con.phoneNumber == creator.phoneNumber }) {
                                if (!sortedList.contains(task)) {
                                    sortedList.add(task)
                                }
                            }
                        }
                    }

                    else -> {

                    }
                }
            }
        }

        sortedList.distinct().toMutableList()
        return sortedList
    }


    private fun isFilterListEmpty(): Boolean {
        return selectedTagsForFilter.isEmpty() && selectedProjectsForFilter.isEmpty()
                && userConnectionAndRoleList.first.isEmpty() && userConnectionAndRoleList.second.isEmpty()
    }


    suspend fun applySortingOrder(list: MutableList<CeibroTaskV2>): MutableList<CeibroTaskV2> {
        var updatedTaskList: MutableList<CeibroTaskV2> = mutableListOf()
        GlobalScope.launch {
            if (list.isNotEmpty()) {
                if (lastSortingType.value.equals("SortByActivity", true)) {
                    val allTasks = list
                    allTasks.sortByDescending { it.updatedAt }
                    updatedTaskList = allTasks

                } else if (lastSortingType.value.equals("SortByUnseen", true)) {
                    val allTasks = list
                    allTasks.sortByDescending { !it.isSeenByMe }
                    updatedTaskList = allTasks

                } else if (lastSortingType.value.equals("SortByNewTask", true)) {
                    val allTasks = list
                    allTasks.sortByDescending { it.createdAt }
                    updatedTaskList = allTasks

                } else if (lastSortingType.value.equals("SortByProject", true)) {
                    val allTasks = list
//                    allTasks.sortByDescending {
//                        when {
//                            it.project != null && it.project.title.firstOrNull()?.isLetter() == true -> 0
//                            it.project != null && it.project.title.firstOrNull()?.isDigit() == true -> 1 // Titles starting with numbers next
//                            it.project != null && it.project.title.isEmpty() -> 2 // Alphabetical titles first
//                            else -> 3 // Tasks with no project title last
//                        }
//                    }

                    // For the first position (alphabetical titles), sort alphabetically by project title
                    val position1Tasks =
                        allTasks.filter { it.project?.title?.firstOrNull()?.isLetter() == true }
                            .toMutableList()
                    position1Tasks.sortBy { it.project?.title?.lowercase() }

                    // Now, let's group the tasks with project titles starting with numbers (position 2)
                    val position2Tasks =
                        allTasks.filter { it.project?.title?.firstOrNull()?.isDigit() == true }
                            .toMutableList()
                    position2Tasks.sortBy { it.project?.title }

                    // For the third position (tasks with no project title), there's no need to sort
                    // Combine all tasks in the desired order
                    val sortedTasks =
                        position1Tasks + position2Tasks + allTasks.filter { it.project == null || it.project.title.isBlank() }

                    updatedTaskList = sortedTasks.toMutableList()

                } else if (lastSortingType.value.equals("SortByDueDate", true)) {
//                loading(true)
                    val currentDate = Date()
                    val allTasks = list

                    val comparator = compareBy<CeibroTaskV2> { task ->
                        when {
                            task.dueDate.isEmpty() -> 3 // Tasks with no due date go to the bottom
                            parseDate(task.dueDate)!! < currentDate -> 2 // Tasks with due date before current date
                            parseDate(task.dueDate)!! > currentDate -> 1 // Tasks with due date after current date
                            else -> 0 // Tasks with due date equal to current date
                        }
                    }.thenByDescending { task ->
                        when {
                            task.dueDate.isEmpty() -> "" // No further sorting for tasks with no due date
                            parseDate(task.dueDate)!! < currentDate -> parseDate(task.dueDate) // Sort tasks with due date before current date in descending order
                            parseDate(task.dueDate)!! > currentDate -> parseDate(task.dueDate) // Sort tasks with due date after current date in descending order
                            else -> "" // No further sorting for tasks with due date equal to current date
                        }
                    }

                    // Sort tasks using the custom comparator
                    val sortedTasksList = allTasks.sortedWith(comparator)

                    updatedTaskList = sortedTasksList.toMutableList()

                } else {
                    val allTasks = list
                    allTasks.sortByDescending { it.createdAt }
                    updatedTaskList = allTasks
                }
            }
        }.join()
        return updatedTaskList
    }

    private fun parseDate(dateString: String): Date? {
        val dateFormatDot = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val dateFormatHyphen = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

        try {
            return dateFormatDot.parse(dateString)
        } catch (e: Exception) {
            // If parsing with "." format fails, try parsing with "-" format
            try {
                return dateFormatHyphen.parse(dateString)
            } catch (e: Exception) {
                // If parsing with "-" format also fails, return null or handle the error as needed
                return null
            }
        }
    }

    fun filterTasksList(query: String) {

        if (query.isEmpty()) {
            isSearchingTasks = false
            _setFilteredDataToCanceledAdapter.postValue(filteredCanceledTasks)
            return
        }
        isSearchingTasks = true
        val filteredCanceledTasks1 =
            filteredCanceledTasks.filter {
                (it.title != null && it.title.contains(query.trim(), true)) ||
                        it.description.contains(query.trim(), true) ||
                        it.taskUID.contains(query.trim(), true) ||
                        (it.project != null && it.project.title.contains(query.trim(), true)) ||
                        it.assignedToState.any { assignee ->
                            assignee.firstName.contains(
                                query.trim(),
                                true
                            ) || assignee.surName.contains(query.trim(), true)
                        }
            }.toMutableList()

        _setFilteredDataToCanceledAdapter.postValue(filteredCanceledTasks1)
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
                            sessionManager,
                            drawingPinsDao
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

}