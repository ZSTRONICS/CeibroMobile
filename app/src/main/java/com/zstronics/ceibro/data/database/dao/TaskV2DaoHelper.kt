package com.zstronics.ceibro.data.database.dao

import com.zstronics.ceibro.data.repos.task.TaskRootStateTags
import com.zstronics.ceibro.data.repos.task.models.SingleTaskEntity
import com.zstronics.ceibro.data.repos.task.models.TaskV2Response
import com.zstronics.ceibro.data.repos.task.models.TasksV2DatabaseEntity
import com.zstronics.ceibro.data.repos.task.models.TasksV2DatabaseEntitySingle
import com.zstronics.ceibro.ui.tasks.task.TaskStatus
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class TaskV2DaoHelper constructor(_taskDao: TaskV2Dao) {
    val taskDao: TaskV2Dao = _taskDao

    suspend fun getTasks(rootState: String): TasksV2DatabaseEntity {
        var entity: TasksV2DatabaseEntity = TasksV2DatabaseEntity(
            rootState = TaskRootStateTags.Default.tagValue,
            allTasks = TaskV2Response.AllTasks(
                new = mutableListOf(),
                unread = mutableListOf(),
                ongoing = mutableListOf(),
                done = mutableListOf(),
                canceled = mutableListOf()
            )
        )
        GlobalScope.launch {
            when (rootState) {
                TaskRootStateTags.ToMe.tagValue -> {
                    var newTask: TasksV2DatabaseEntitySingle? = null
                    var ongoingTask: TasksV2DatabaseEntitySingle? = null
                    var doneTask: TasksV2DatabaseEntitySingle? = null

                    GlobalScope.launch {
                        newTask = taskDao.getTasksByState(rootState, TaskStatus.NEW.name)
                    }.join()
                    GlobalScope.launch {
                        ongoingTask = taskDao.getTasksByState(rootState, TaskStatus.ONGOING.name)
                    }.join()
                    GlobalScope.launch {
                        doneTask = taskDao.getTasksByState(rootState, TaskStatus.DONE.name)
                    }.join()


                    val allTasksList = TaskV2Response.AllTasks(
                        new = newTask?.task?.data ?: mutableListOf(),
                        ongoing = ongoingTask?.task?.data ?: mutableListOf(),
                        done = doneTask?.task?.data ?: mutableListOf(),
                        unread = mutableListOf()
                    )

                    entity = TasksV2DatabaseEntity(
                        rootState = rootState,
                        allTasks = allTasksList
                    )
                }

                TaskRootStateTags.FromMe.tagValue -> {

                    var unreadTask: TasksV2DatabaseEntitySingle? = null
                    var ongoingTask: TasksV2DatabaseEntitySingle? = null
                    var doneTask: TasksV2DatabaseEntitySingle? = null

                    GlobalScope.launch {
                        unreadTask = taskDao.getTasksByState(rootState, TaskStatus.UNREAD.name)
                    }.join()
                    GlobalScope.launch {
                        ongoingTask = taskDao.getTasksByState(rootState, TaskStatus.ONGOING.name)
                    }.join()
                    GlobalScope.launch {
                        doneTask = taskDao.getTasksByState(rootState, TaskStatus.DONE.name)
                    }.join()


                    val allTasksList = TaskV2Response.AllTasks(
                        new = mutableListOf(),
                        ongoing = ongoingTask?.task?.data ?: mutableListOf(),
                        done = doneTask?.task?.data ?: mutableListOf(),
                        unread = unreadTask?.task?.data ?: mutableListOf()
                    )

                    entity = TasksV2DatabaseEntity(
                        rootState = rootState,
                        allTasks = allTasksList
                    )
                }

                TaskRootStateTags.Hidden.tagValue -> {

                    var canceledTask: TasksV2DatabaseEntitySingle? = null
                    var ongoingTask: TasksV2DatabaseEntitySingle? = null
                    var doneTask: TasksV2DatabaseEntitySingle? = null


                    GlobalScope.launch {
                        canceledTask = taskDao.getTasksByState(rootState, TaskStatus.CANCELED.name)
                    }.join()
                    GlobalScope.launch {
                        ongoingTask = taskDao.getTasksByState(rootState, TaskStatus.ONGOING.name)
                    }.join()
                    GlobalScope.launch {
                        doneTask = taskDao.getTasksByState(rootState, TaskStatus.DONE.name)
                    }.join()


                    val allTasksList = TaskV2Response.AllTasks(
                        new = mutableListOf(),
                        unread = mutableListOf(),
                        ongoing = ongoingTask?.task?.data ?: mutableListOf(),
                        done = doneTask?.task?.data ?: mutableListOf(),
                        canceled = canceledTask?.task?.data ?: mutableListOf()
                    )

                    entity = TasksV2DatabaseEntity(
                        rootState = rootState,
                        allTasks = allTasksList
                    )
                }
            }
        }.join()
        return entity
    }

    fun isTaskListEmpty(rootState: String, taskList: TasksV2DatabaseEntity): Boolean {

        var listEmpty = false
        when (rootState) {
            TaskRootStateTags.ToMe.tagValue -> {
                listEmpty =
                    taskList.allTasks.new.isEmpty() && taskList.allTasks.ongoing.isEmpty() && taskList.allTasks.done.isEmpty()
            }

            TaskRootStateTags.FromMe.tagValue -> {
                listEmpty =
                    taskList.allTasks.unread.isEmpty() && taskList.allTasks.ongoing.isEmpty() && taskList.allTasks.done.isEmpty()
            }

            TaskRootStateTags.Hidden.tagValue -> {
                listEmpty =
                    taskList.allTasks.canceled.isEmpty() && taskList.allTasks.ongoing.isEmpty() && taskList.allTasks.done.isEmpty()
            }
        }
        return listEmpty
    }

    @OptIn(DelicateCoroutinesApi::class)
    suspend fun insertTaskData(task: TasksV2DatabaseEntity) {
        GlobalScope.launch {
            when (task.rootState) {
                TaskRootStateTags.ToMe.tagValue -> {

                    GlobalScope.launch {
                        println("TaskListInsert -> To-Me = New ${task.allTasks.new.size}")
                        taskDao.insertTaskDataWithState(
                            TasksV2DatabaseEntitySingle(
                                rootState = task.rootState,
                                subState = TaskStatus.NEW.name,
                                task = SingleTaskEntity(task.allTasks.new)
                            )
                        )
                    }.join()
                    GlobalScope.launch {
                        println("TaskListInsert -> To-Me = ongoing ${task.allTasks.ongoing.size}")
                        taskDao.insertTaskDataWithState(
                            TasksV2DatabaseEntitySingle(
                                rootState = task.rootState,
                                subState = TaskStatus.ONGOING.name,
                                task = SingleTaskEntity(task.allTasks.ongoing)
                            )
                        )
                    }.join()
                    GlobalScope.launch {
                        println("TaskListInsert -> To-Me = DONE ${task.allTasks.done.size}")
                        taskDao.insertTaskDataWithState(
                            TasksV2DatabaseEntitySingle(
                                rootState = task.rootState,
                                subState = TaskStatus.DONE.name,
                                task = SingleTaskEntity(task.allTasks.done)
                            )
                        )
                    }.join()
                }


                TaskRootStateTags.FromMe.tagValue -> {
                    GlobalScope.launch {
                        taskDao.insertTaskDataWithState(
                            TasksV2DatabaseEntitySingle(
                                rootState = task.rootState,
                                subState = TaskStatus.UNREAD.name,
                                task = SingleTaskEntity(task.allTasks.unread)
                            )
                        )
                    }.join()
                    GlobalScope.launch {
                        taskDao.insertTaskDataWithState(
                            TasksV2DatabaseEntitySingle(
                                rootState = task.rootState,
                                subState = TaskStatus.ONGOING.name,
                                task = SingleTaskEntity(task.allTasks.ongoing)
                            )
                        )
                    }.join()
                    GlobalScope.launch {
                        taskDao.insertTaskDataWithState(
                            TasksV2DatabaseEntitySingle(
                                rootState = task.rootState,
                                subState = TaskStatus.DONE.name,
                                task = SingleTaskEntity(task.allTasks.done)
                            )
                        )
                    }.join()
                }

                TaskRootStateTags.Hidden.tagValue -> {
                    GlobalScope.launch {
                        taskDao.insertTaskDataWithState(
                            TasksV2DatabaseEntitySingle(
                                rootState = task.rootState,
                                subState = TaskStatus.ONGOING.name,
                                task = SingleTaskEntity(task.allTasks.ongoing)
                            )
                        )
                    }.join()
                    GlobalScope.launch {
                        taskDao.insertTaskDataWithState(
                            TasksV2DatabaseEntitySingle(
                                rootState = task.rootState,
                                subState = TaskStatus.DONE.name,
                                task = SingleTaskEntity(task.allTasks.done)
                            )
                        )
                    }.join()
                    GlobalScope.launch {
                        taskDao.insertTaskDataWithState(
                            TasksV2DatabaseEntitySingle(
                                rootState = task.rootState,
                                subState = TaskStatus.CANCELED.name,
                                task = SingleTaskEntity(task.allTasks.canceled)
                            )
                        )
                    }.join()
                }
            }
        }.join()
    }
}