package com.zstronics.ceibro.ui.inbox

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.CeibroApplication
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.database.dao.DrawingPinsV2Dao
import com.zstronics.ceibro.data.database.dao.InboxV2Dao
import com.zstronics.ceibro.data.database.dao.TaskV2Dao
import com.zstronics.ceibro.data.database.models.inbox.CeibroInboxV2
import com.zstronics.ceibro.data.repos.task.ITaskRepository
import com.zstronics.ceibro.data.repos.task.models.v2.TaskSeenResponse
import com.zstronics.ceibro.data.sessions.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class InboxVM @Inject constructor(
    override val viewState: InboxState,
    val sessionManager: SessionManager,
    private val taskRepository: ITaskRepository,
    val inboxV2Dao: InboxV2Dao,
    val drawingPinsDao: DrawingPinsV2Dao,
    val taskDao: TaskV2Dao,
) : HiltBaseViewModel<IInbox.State>(), IInbox.ViewModel {
    val user = sessionManager.getUserObj()

    val _inboxTasks: MutableLiveData<MutableList<CeibroInboxV2>> = MutableLiveData()
    val inboxTasks: MutableLiveData<MutableList<CeibroInboxV2>> = _inboxTasks
    var originalInboxTasks: MutableList<CeibroInboxV2> = mutableListOf()

    val _filteredInboxTasks: MutableLiveData<MutableList<CeibroInboxV2>> = MutableLiveData()
    val filteredInboxTasks: MutableLiveData<MutableList<CeibroInboxV2>> = _filteredInboxTasks

    var isUserSearching = false
    var lastSortingType = "SortByActivity"
    var fragmentFirstRun = true

    override fun onFirsTimeUiCreate(bundle: Bundle?) {
        super.onFirsTimeUiCreate(bundle)
        launch {
//            val allInboxTasks = inboxV2Dao.getAllInboxItems().toMutableList()
//            CeibroApplication.CookiesManager.allInboxTasks.postValue(allInboxTasks)
//            _inboxTasks.postValue(allInboxTasks)
        }
    }

    fun deleteInboxTaskFromDB(originalTaskToRemove: CeibroInboxV2) {
        GlobalScope.launch {
            inboxV2Dao.deleteInboxTaskData(originalTaskToRemove.taskId)
            val allInboxTasks = inboxV2Dao.getAllInboxItems().toMutableList()
            withContext(Dispatchers.Main) {
                CeibroApplication.CookiesManager.allInboxTasks.postValue(allInboxTasks)
            }
        }
    }

    fun changeSortingOrder(latestSortingType: String) {
        GlobalScope.launch {

            if (latestSortingType.equals("SortByActivity", true)) {
                val allTasks = originalInboxTasks
                allTasks.sortByDescending { it.createdAt }
                originalInboxTasks = allTasks
                _inboxTasks.postValue(allTasks)
                isUserSearching = false
                withContext(Dispatchers.Main) {
                    CeibroApplication.CookiesManager.allInboxTasks.postValue(allTasks)
                    CeibroApplication.CookiesManager.inboxTasksSortingType.postValue("SortByActivity")
                }

            } else if (latestSortingType.equals("SortByUnread", true)) {
                val allTasks = originalInboxTasks
                allTasks.sortByDescending { !it.isSeen }
                originalInboxTasks = allTasks
                _inboxTasks.postValue(allTasks)
                isUserSearching = false
                withContext(Dispatchers.Main) {
                    CeibroApplication.CookiesManager.allInboxTasks.postValue(allTasks)
                    CeibroApplication.CookiesManager.inboxTasksSortingType.postValue("SortByUnread")
                }

            } else if (latestSortingType.equals("SortByDueDate", true)) {
                loading(true)
                val currentDate = Date()
                val allTasks = originalInboxTasks

                val comparator = compareBy<CeibroInboxV2> { task ->
                    when {
                        task.actionDataTask.dueDate.isEmpty() -> 3 // Tasks with no due date go to the bottom
                        parseDate(task.actionDataTask.dueDate)!! < currentDate -> 2 // Tasks with due date before current date
                        parseDate(task.actionDataTask.dueDate)!! > currentDate -> 1 // Tasks with due date after current date
                        else -> 0 // Tasks with due date equal to current date
                    }
                }.thenByDescending { task ->
                    when {
                        task.actionDataTask.dueDate.isEmpty() -> "" // No further sorting for tasks with no due date
                        parseDate(task.actionDataTask.dueDate)!! < currentDate -> parseDate(task.actionDataTask.dueDate) // Sort tasks with due date before current date in descending order
                        parseDate(task.actionDataTask.dueDate)!! > currentDate -> parseDate(task.actionDataTask.dueDate) // Sort tasks with due date after current date in descending order
                        else -> "" // No further sorting for tasks with due date equal to current date
                    }
                }

                // Sort tasks using the custom comparator
                val sortedTasksList = allTasks.sortedWith(comparator)

                originalInboxTasks = sortedTasksList.toMutableList()
                _inboxTasks.postValue(sortedTasksList.toMutableList())
                isUserSearching = false
                loading(false, "")
                withContext(Dispatchers.Main) {
                    CeibroApplication.CookiesManager.allInboxTasks.postValue(sortedTasksList.toMutableList())
                    CeibroApplication.CookiesManager.inboxTasksSortingType.postValue("SortByDueDate")
                }

            } else {
                val allTasks = originalInboxTasks
                allTasks.sortByDescending { it.createdAt }
                originalInboxTasks = allTasks
                _inboxTasks.postValue(allTasks)
                isUserSearching = false
                withContext(Dispatchers.Main) {
                    CeibroApplication.CookiesManager.allInboxTasks.postValue(allTasks)
                    CeibroApplication.CookiesManager.inboxTasksSortingType.postValue("SortByActivity")
                }
            }
        }
    }

    fun changeSortingOrderWhenNewItemAdded(
        allInboxTasks: MutableList<CeibroInboxV2>,
        latestSortingType: String
    ) {
        GlobalScope.launch {

            if (latestSortingType.equals("SortByActivity", true)) {
                val allTasks = allInboxTasks
                allTasks.sortByDescending { it.createdAt }
                originalInboxTasks = allTasks
                _inboxTasks.postValue(allTasks)
                isUserSearching = false
                withContext(Dispatchers.Main) {
                    CeibroApplication.CookiesManager.allInboxTasks.postValue(allTasks)
                    CeibroApplication.CookiesManager.inboxTasksSortingType.postValue("SortByActivity")
                }

            } else if (latestSortingType.equals("SortByUnread", true)) {
                val allTasks = allInboxTasks
                allTasks.sortByDescending { !it.isSeen }
                originalInboxTasks = allTasks
                _inboxTasks.postValue(allTasks)
                isUserSearching = false
                withContext(Dispatchers.Main) {
                    CeibroApplication.CookiesManager.allInboxTasks.postValue(allTasks)
                    CeibroApplication.CookiesManager.inboxTasksSortingType.postValue("SortByUnread")
                }

            } else if (latestSortingType.equals("SortByDueDate", true)) {
//                loading(true)
                val currentDate = Date()
                val allTasks = allInboxTasks

                val comparator = compareBy<CeibroInboxV2> { task ->
                    when {
                        task.actionDataTask.dueDate.isEmpty() -> 3 // Tasks with no due date go to the bottom
                        parseDate(task.actionDataTask.dueDate)!! < currentDate -> 2 // Tasks with due date before current date
                        parseDate(task.actionDataTask.dueDate)!! > currentDate -> 1 // Tasks with due date after current date
                        else -> 0 // Tasks with due date equal to current date
                    }
                }.thenByDescending { task ->
                    when {
                        task.actionDataTask.dueDate.isEmpty() -> "" // No further sorting for tasks with no due date
                        parseDate(task.actionDataTask.dueDate)!! < currentDate -> parseDate(task.actionDataTask.dueDate) // Sort tasks with due date before current date in descending order
                        parseDate(task.actionDataTask.dueDate)!! > currentDate -> parseDate(task.actionDataTask.dueDate) // Sort tasks with due date after current date in descending order
                        else -> "" // No further sorting for tasks with due date equal to current date
                    }
                }

                // Sort tasks using the custom comparator
                val sortedTasksList = allTasks.sortedWith(comparator)

                originalInboxTasks = sortedTasksList.toMutableList()
                _inboxTasks.postValue(sortedTasksList.toMutableList())
                isUserSearching = false
//                loading(false, "")
                withContext(Dispatchers.Main) {
                    CeibroApplication.CookiesManager.allInboxTasks.postValue(sortedTasksList.toMutableList())
                    CeibroApplication.CookiesManager.inboxTasksSortingType.postValue("SortByDueDate")
                }

            } else {
                val allTasks = allInboxTasks
                allTasks.sortByDescending { it.createdAt }
                originalInboxTasks = allTasks
                _inboxTasks.postValue(allTasks)
                isUserSearching = false
                withContext(Dispatchers.Main) {
                    CeibroApplication.CookiesManager.allInboxTasks.postValue(allTasks)
                    CeibroApplication.CookiesManager.inboxTasksSortingType.postValue("SortByActivity")
                }
            }
        }
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

    fun searchInboxTasks(query: String) {
        if (query.isEmpty()) {
            isUserSearching = false
            _inboxTasks.postValue(originalInboxTasks)
            return
        }
        isUserSearching = true

        val filteredTasks = originalInboxTasks.filter {
            (it.actionTitle.isNotEmpty() && it.actionTitle.contains(query, true)) ||
                    (it.actionDescription.isNotEmpty() && it.actionDescription.contains(
                        query,
                        true
                    )) ||
                    (it.actionDataTask.taskUID.isNotEmpty() && it.actionDataTask.taskUID.contains(
                        query,
                        true
                    )) ||
                    (it.actionDataTask.project != null && it.actionDataTask.project.title.contains(
                        query,
                        true
                    )) ||
                    "${it.actionBy.firstName} ${it.actionBy.surName}".contains(query, true)

        }.toMutableList()

        _filteredInboxTasks.postValue(filteredTasks)
    }


    fun taskSeen(
        taskId: String,
        onBack: (taskSeenData: TaskSeenResponse.TaskSeen) -> Unit,
    ) {
        launch {
            //loading(true)
            taskRepository.taskSeen(taskId) { isSuccess, taskSeenData ->
                if (isSuccess) {
//                    println("Heartbeat taskSeenData: ${taskSeenData}")
                    if (taskSeenData != null) {
                        launch {
                            updateGenericTaskSeenInLocal(
                                taskSeenData,
                                taskDao,
                                user?.id,
                                sessionManager,
                                drawingPinsDao,
                                inboxV2Dao
                            )
                        }
                        onBack(taskSeenData)
                    }

                } else {
//                    println("Heartbeat taskSeenData: ${taskSeenData}")
//                    loading(false, "")
                }
            }
        }
    }

}