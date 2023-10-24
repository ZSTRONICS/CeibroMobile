package com.zstronics.ceibro.data.database.dao

import com.zstronics.ceibro.data.database.models.tasks.CeibroTaskV2
import com.zstronics.ceibro.data.database.models.tasks.Events
import com.zstronics.ceibro.data.repos.task.TaskRootStateTags
import com.zstronics.ceibro.data.repos.task.models.SingleTaskEntity
import com.zstronics.ceibro.data.repos.task.models.TaskV2Response
import com.zstronics.ceibro.data.repos.task.models.TasksV2DatabaseEntity
import com.zstronics.ceibro.data.repos.task.models.TasksV2DatabaseEntitySingle
import com.zstronics.ceibro.ui.tasks.task.TaskStatus
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class TaskV2DaoHelper constructor(_taskDao: TaskV2Dao) {
    val taskDao: TaskV2Dao = _taskDao

    suspend fun getTasks(rootState: String): TasksV2DatabaseEntity {
        return coroutineScope {
            val entity: TasksV2DatabaseEntity = TasksV2DatabaseEntity(
                rootState = rootState,
                allTasks = TaskV2Response.AllTasks(
                    new = mutableListOf(),
                    unread = mutableListOf(),
                    ongoing = mutableListOf(),
                    done = mutableListOf(),
                    canceled = mutableListOf()
                )
            )

            val allTasksList = when (rootState) {
                TaskRootStateTags.ToMe.tagValue -> {
                    val newTaskDeferred =
                        async { taskDao.getTasksByState(rootState, TaskStatus.NEW.name) }
                    val ongoingTaskDeferred =
                        async { taskDao.getTasksByState(rootState, TaskStatus.ONGOING.name) }
                    val doneTaskDeferred =
                        async { taskDao.getTasksByState(rootState, TaskStatus.DONE.name) }

                    val newTask = newTaskDeferred.await()?.task?.data ?: mutableListOf()
                    val ongoingTask = ongoingTaskDeferred.await()?.task?.data ?: mutableListOf()
                    val doneTask = doneTaskDeferred.await()?.task?.data ?: mutableListOf()

                    TaskV2Response.AllTasks(
                        new = newTask,
                        ongoing = ongoingTask,
                        done = doneTask,
                        unread = mutableListOf()
                    )
                }

                TaskRootStateTags.FromMe.tagValue -> {
                    val unreadTaskDeferred =
                        async { taskDao.getTasksByState(rootState, TaskStatus.UNREAD.name) }
                    val ongoingTaskDeferred =
                        async { taskDao.getTasksByState(rootState, TaskStatus.ONGOING.name) }
                    val doneTaskDeferred =
                        async { taskDao.getTasksByState(rootState, TaskStatus.DONE.name) }

                    val unreadTask = unreadTaskDeferred.await()?.task?.data ?: mutableListOf()
                    val ongoingTask = ongoingTaskDeferred.await()?.task?.data ?: mutableListOf()
                    val doneTask = doneTaskDeferred.await()?.task?.data ?: mutableListOf()

                    TaskV2Response.AllTasks(
                        new = mutableListOf(),
                        ongoing = ongoingTask,
                        done = doneTask,
                        unread = unreadTask
                    )
                }

                TaskRootStateTags.Hidden.tagValue -> {
                    val canceledTaskDeferred =
                        async { taskDao.getTasksByState(rootState, TaskStatus.CANCELED.name) }
                    val ongoingTaskDeferred =
                        async { taskDao.getTasksByState(rootState, TaskStatus.ONGOING.name) }
                    val doneTaskDeferred =
                        async { taskDao.getTasksByState(rootState, TaskStatus.DONE.name) }

                    val canceledTask = canceledTaskDeferred.await()?.task?.data ?: mutableListOf()
                    val ongoingTask = ongoingTaskDeferred.await()?.task?.data ?: mutableListOf()
                    val doneTask = doneTaskDeferred.await()?.task?.data ?: mutableListOf()

                    TaskV2Response.AllTasks(
                        new = mutableListOf(),
                        unread = mutableListOf(),
                        ongoing = ongoingTask,
                        done = doneTask,
                        canceled = canceledTask
                    )
                }

                else -> TaskV2Response.AllTasks()
            }

            entity.copy(allTasks = allTasksList)
        }
    }


//    fun checkAndPushEventToTask(task: CeibroTaskV2, event: Events): CeibroTaskV2 {
//        var oldEvents = task.events.toMutableList();
//        if (oldEvents.isNotEmpty()) {
//            val oldEventIndex = oldEvents.indexOf(event)
//            if (oldEventIndex != -1) {     //means event already exist, so replace it
//                oldEvents[oldEventIndex] = event
//                task.events = oldEvents
//            } else {
//                oldEvents.add(event)
//                task.events = oldEvents
//            }
//        } else {
//            val taskEventList: MutableList<Events> = mutableListOf()
//            taskEventList.add(event)
//            task.events = taskEventList
//        }
//        return task;
//    }

//    fun checkAndPushEventToTaskArray(taskList: MutableList<CeibroTaskV2>, event: Events): MutableList<CeibroTaskV2> {
//        var isTaskExists = taskList.find { it.id == event.taskId }
//        if (isTaskExists != null) {
//            val taskIndex = taskList.indexOf(isTaskExists)
//            val updatedTask = checkAndPushEventToTask(isTaskExists, event);
//            taskList[taskIndex] = updatedTask;
//        }
//        return taskList;
//    }

//    suspend fun upsertEventInTaskList(rootState: String, subState: String, event: Events) {
//
//        when (rootState) {
//            TaskRootStateTags.ToMe.tagValue -> {
//                when (subState) {
//                    "new" -> {
//                        println("TaskListInsert -> To-Me = New ${task.allTasks.new.size}")
//                        taskDao.insertTaskDataWithState(
//                            TasksV2DatabaseEntitySingle(
//                                rootState = task.rootState,
//                                subState = TaskStatus.NEW.name,
//                                task = SingleTaskEntity(task.allTasks.new)
//                            )
//                        )
//                    }
//
//                    "ongoing" -> {
//                        println("TaskListInsert -> To-Me = ongoing ${task.allTasks.ongoing.size}")
//                        taskDao.insertTaskDataWithState(
//                            TasksV2DatabaseEntitySingle(
//                                rootState = task.rootState,
//                                subState = TaskStatus.ONGOING.name,
//                                task = SingleTaskEntity(task.allTasks.ongoing)
//                            )
//                        )
//                    }
//
//                    "done" -> {
//                        println("TaskListInsert -> To-Me = DONE ${task.allTasks.done.size}")
//                        taskDao.insertTaskDataWithState(
//                            TasksV2DatabaseEntitySingle(
//                                rootState = task.rootState,
//                                subState = TaskStatus.DONE.name,
//                                task = SingleTaskEntity(task.allTasks.done)
//                            )
//                        )
//                    }
//                }
//            }
//
//            TaskRootStateTags.FromMe.tagValue -> {
//                when (subState) {
//                    "unread" -> {
//                        taskDao.insertTaskDataWithState(
//                            TasksV2DatabaseEntitySingle(
//                                rootState = task.rootState,
//                                subState = TaskStatus.UNREAD.name,
//                                task = SingleTaskEntity(task.allTasks.unread)
//                            )
//                        )
//                    }
//
//                    "ongoing" -> {
//                        taskDao.insertTaskDataWithState(
//                            TasksV2DatabaseEntitySingle(
//                                rootState = task.rootState,
//                                subState = TaskStatus.ONGOING.name,
//                                task = SingleTaskEntity(task.allTasks.ongoing)
//                            )
//                        )
//                    }
//
//                    "done" -> {
//                        taskDao.insertTaskDataWithState(
//                            TasksV2DatabaseEntitySingle(
//                                rootState = task.rootState,
//                                subState = TaskStatus.DONE.name,
//                                task = SingleTaskEntity(task.allTasks.done)
//                            )
//                        )
//                    }
//                }
//            }
//
//            TaskRootStateTags.Hidden.tagValue -> {
//                when (subState) {
//                    "ongoing" -> {
//
//                        val taskHiddenLocalData = getTasks(TaskRootStateTags.Hidden.tagValue)
//
//                        taskDao.insertTaskDataWithState(
//                            TasksV2DatabaseEntitySingle(
//                                rootState = task.rootState,
//                                subState = TaskStatus.ONGOING.name,
//                                task = SingleTaskEntity(task.allTasks.ongoing)
//                            )
//                        )
//                    }
//
//                    "done" -> {
//                        taskDao.insertTaskDataWithState(
//                            TasksV2DatabaseEntitySingle(
//                                rootState = task.rootState,
//                                subState = TaskStatus.DONE.name,
//                                task = SingleTaskEntity(task.allTasks.done)
//                            )
//                        )
//                    }
//
//                    "canceled" -> {
//                        taskDao.insertTaskDataWithState(
//                            TasksV2DatabaseEntitySingle(
//                                rootState = task.rootState,
//                                subState = TaskStatus.CANCELED.name,
//                                task = SingleTaskEntity(task.allTasks.canceled)
//                            )
//                        )
//                    }
//                }
//            }
//        }
//    }

//    suspend fun getTasks(rootState: String): TasksV2DatabaseEntity {
//        var entity: TasksV2DatabaseEntity = TasksV2DatabaseEntity(
//            rootState = TaskRootStateTags.Default.tagValue,
//            allTasks = TaskV2Response.AllTasks(
//                new = mutableListOf(),
//                unread = mutableListOf(),
//                ongoing = mutableListOf(),
//                done = mutableListOf(),
//                canceled = mutableListOf()
//            )
//        )
//        GlobalScope.launch {
//            when (rootState) {
//                TaskRootStateTags.ToMe.tagValue -> {
//                    var newTask: TasksV2DatabaseEntitySingle? = null
//                    var ongoingTask: TasksV2DatabaseEntitySingle? = null
//                    var doneTask: TasksV2DatabaseEntitySingle? = null
//
//                    GlobalScope.launch {
//                        newTask = taskDao.getTasksByState(rootState, TaskStatus.NEW.name)
//                    }.join()
//                    GlobalScope.launch {
//                        ongoingTask = taskDao.getTasksByState(rootState, TaskStatus.ONGOING.name)
//                    }.join()
//                    GlobalScope.launch {
//                        doneTask = taskDao.getTasksByState(rootState, TaskStatus.DONE.name)
//                    }.join()
//
//
//                    val allTasksList = TaskV2Response.AllTasks(
//                        new = newTask?.task?.data ?: mutableListOf(),
//                        ongoing = ongoingTask?.task?.data ?: mutableListOf(),
//                        done = doneTask?.task?.data ?: mutableListOf(),
//                        unread = mutableListOf()
//                    )
//
//                    entity = TasksV2DatabaseEntity(
//                        rootState = rootState,
//                        allTasks = allTasksList
//                    )
//                }
//
//                TaskRootStateTags.FromMe.tagValue -> {
//
//                    var unreadTask: TasksV2DatabaseEntitySingle? = null
//                    var ongoingTask: TasksV2DatabaseEntitySingle? = null
//                    var doneTask: TasksV2DatabaseEntitySingle? = null
//
//                    GlobalScope.launch {
//                        unreadTask = taskDao.getTasksByState(rootState, TaskStatus.UNREAD.name)
//                    }.join()
//                    GlobalScope.launch {
//                        ongoingTask = taskDao.getTasksByState(rootState, TaskStatus.ONGOING.name)
//                    }.join()
//                    GlobalScope.launch {
//                        doneTask = taskDao.getTasksByState(rootState, TaskStatus.DONE.name)
//                    }.join()
//
//
//                    val allTasksList = TaskV2Response.AllTasks(
//                        new = mutableListOf(),
//                        ongoing = ongoingTask?.task?.data ?: mutableListOf(),
//                        done = doneTask?.task?.data ?: mutableListOf(),
//                        unread = unreadTask?.task?.data ?: mutableListOf()
//                    )
//
//                    entity = TasksV2DatabaseEntity(
//                        rootState = rootState,
//                        allTasks = allTasksList
//                    )
//                }
//
//                TaskRootStateTags.Hidden.tagValue -> {
//
//                    var canceledTask: TasksV2DatabaseEntitySingle? = null
//                    var ongoingTask: TasksV2DatabaseEntitySingle? = null
//                    var doneTask: TasksV2DatabaseEntitySingle? = null
//
//
//                    GlobalScope.launch {
//                        canceledTask = taskDao.getTasksByState(rootState, TaskStatus.CANCELED.name)
//                    }.join()
//                    GlobalScope.launch {
//                        ongoingTask = taskDao.getTasksByState(rootState, TaskStatus.ONGOING.name)
//                    }.join()
//                    GlobalScope.launch {
//                        doneTask = taskDao.getTasksByState(rootState, TaskStatus.DONE.name)
//                    }.join()
//
//
//                    val allTasksList = TaskV2Response.AllTasks(
//                        new = mutableListOf(),
//                        unread = mutableListOf(),
//                        ongoing = ongoingTask?.task?.data ?: mutableListOf(),
//                        done = doneTask?.task?.data ?: mutableListOf(),
//                        canceled = canceledTask?.task?.data ?: mutableListOf()
//                    )
//
//                    entity = TasksV2DatabaseEntity(
//                        rootState = rootState,
//                        allTasks = allTasksList
//                    )
//                }
//            }
//        }.join()
//        return entity
//    }

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

//    @OptIn(DelicateCoroutinesApi::class)
//    suspend fun insertTaskData(task: TasksV2DatabaseEntity) {
//        GlobalScope.launch {
//            when (task.rootState) {
//                TaskRootStateTags.ToMe.tagValue -> {
//
//                    GlobalScope.launch {
//                        println("TaskListInsert -> To-Me = New ${task.allTasks.new.size}")
//                        taskDao.insertTaskDataWithState(
//                            TasksV2DatabaseEntitySingle(
//                                rootState = task.rootState,
//                                subState = TaskStatus.NEW.name,
//                                task = SingleTaskEntity(task.allTasks.new)
//                            )
//                        )
//                    }.join()
//                    GlobalScope.launch {
//                        println("TaskListInsert -> To-Me = ongoing ${task.allTasks.ongoing.size}")
//                        taskDao.insertTaskDataWithState(
//                            TasksV2DatabaseEntitySingle(
//                                rootState = task.rootState,
//                                subState = TaskStatus.ONGOING.name,
//                                task = SingleTaskEntity(task.allTasks.ongoing)
//                            )
//                        )
//                    }.join()
//                    GlobalScope.launch {
//                        println("TaskListInsert -> To-Me = DONE ${task.allTasks.done.size}")
//                        taskDao.insertTaskDataWithState(
//                            TasksV2DatabaseEntitySingle(
//                                rootState = task.rootState,
//                                subState = TaskStatus.DONE.name,
//                                task = SingleTaskEntity(task.allTasks.done)
//                            )
//                        )
//                    }.join()
//                }
//
//                TaskRootStateTags.FromMe.tagValue -> {
//                    GlobalScope.launch {
//                        taskDao.insertTaskDataWithState(
//                            TasksV2DatabaseEntitySingle(
//                                rootState = task.rootState,
//                                subState = TaskStatus.UNREAD.name,
//                                task = SingleTaskEntity(task.allTasks.unread)
//                            )
//                        )
//                    }.join()
//                    GlobalScope.launch {
//                        taskDao.insertTaskDataWithState(
//                            TasksV2DatabaseEntitySingle(
//                                rootState = task.rootState,
//                                subState = TaskStatus.ONGOING.name,
//                                task = SingleTaskEntity(task.allTasks.ongoing)
//                            )
//                        )
//                    }.join()
//                    GlobalScope.launch {
//                        taskDao.insertTaskDataWithState(
//                            TasksV2DatabaseEntitySingle(
//                                rootState = task.rootState,
//                                subState = TaskStatus.DONE.name,
//                                task = SingleTaskEntity(task.allTasks.done)
//                            )
//                        )
//                    }.join()
//                }
//
//                TaskRootStateTags.Hidden.tagValue -> {
//                    GlobalScope.launch {
//                        taskDao.insertTaskDataWithState(
//                            TasksV2DatabaseEntitySingle(
//                                rootState = task.rootState,
//                                subState = TaskStatus.ONGOING.name,
//                                task = SingleTaskEntity(task.allTasks.ongoing)
//                            )
//                        )
//                    }.join()
//                    GlobalScope.launch {
//                        taskDao.insertTaskDataWithState(
//                            TasksV2DatabaseEntitySingle(
//                                rootState = task.rootState,
//                                subState = TaskStatus.DONE.name,
//                                task = SingleTaskEntity(task.allTasks.done)
//                            )
//                        )
//                    }.join()
//                    GlobalScope.launch {
//                        taskDao.insertTaskDataWithState(
//                            TasksV2DatabaseEntitySingle(
//                                rootState = task.rootState,
//                                subState = TaskStatus.CANCELED.name,
//                                task = SingleTaskEntity(task.allTasks.canceled)
//                            )
//                        )
//                    }.join()
//                }
//            }
//        }.join()
//    }


    @OptIn(DelicateCoroutinesApi::class)
    suspend fun insertTaskData(task: TasksV2DatabaseEntity) {
        coroutineScope {
            when (task.rootState) {
                TaskRootStateTags.ToMe.tagValue -> {
                    val newTaskDeferred = async {
                        println("TaskListInsert -> To-Me = New ${task.allTasks.new.size}")
                        taskDao.insertTaskDataWithState(
                            TasksV2DatabaseEntitySingle(
                                rootState = task.rootState,
                                subState = TaskStatus.NEW.name,
                                task = SingleTaskEntity(task.allTasks.new)
                            )
                        )
                    }
                    val ongoingTaskDeferred = async {
                        println("TaskListInsert -> To-Me = Ongoing ${task.allTasks.ongoing.size}")
                        taskDao.insertTaskDataWithState(
                            TasksV2DatabaseEntitySingle(
                                rootState = task.rootState,
                                subState = TaskStatus.ONGOING.name,
                                task = SingleTaskEntity(task.allTasks.ongoing)
                            )
                        )
                    }
                    val doneTaskDeferred = async {
                        println("TaskListInsert -> To-Me = DONE ${task.allTasks.done.size}")
                        taskDao.insertTaskDataWithState(
                            TasksV2DatabaseEntitySingle(
                                rootState = task.rootState,
                                subState = TaskStatus.DONE.name,
                                task = SingleTaskEntity(task.allTasks.done)
                            )
                        )
                    }

                    newTaskDeferred.await()
                    ongoingTaskDeferred.await()
                    doneTaskDeferred.await()
                }

                TaskRootStateTags.FromMe.tagValue -> {
                    val unreadTaskDeferred = async {
                        taskDao.insertTaskDataWithState(
                            TasksV2DatabaseEntitySingle(
                                rootState = task.rootState,
                                subState = TaskStatus.UNREAD.name,
                                task = SingleTaskEntity(task.allTasks.unread)
                            )
                        )
                    }
                    val ongoingTaskDeferred = async {
                        taskDao.insertTaskDataWithState(
                            TasksV2DatabaseEntitySingle(
                                rootState = task.rootState,
                                subState = TaskStatus.ONGOING.name,
                                task = SingleTaskEntity(task.allTasks.ongoing)
                            )
                        )
                    }
                    val doneTaskDeferred = async {
                        taskDao.insertTaskDataWithState(
                            TasksV2DatabaseEntitySingle(
                                rootState = task.rootState,
                                subState = TaskStatus.DONE.name,
                                task = SingleTaskEntity(task.allTasks.done)
                            )
                        )
                    }

                    unreadTaskDeferred.await()
                    ongoingTaskDeferred.await()
                    doneTaskDeferred.await()
                }

                TaskRootStateTags.Hidden.tagValue -> {
                    val ongoingTaskDeferred = async {
                        taskDao.insertTaskDataWithState(
                            TasksV2DatabaseEntitySingle(
                                rootState = task.rootState,
                                subState = TaskStatus.ONGOING.name,
                                task = SingleTaskEntity(task.allTasks.ongoing)
                            )
                        )
                    }
                    val doneTaskDeferred = async {
                        taskDao.insertTaskDataWithState(
                            TasksV2DatabaseEntitySingle(
                                rootState = task.rootState,
                                subState = TaskStatus.DONE.name,
                                task = SingleTaskEntity(task.allTasks.done)
                            )
                        )
                    }
                    val canceledTaskDeferred = async {
                        taskDao.insertTaskDataWithState(
                            TasksV2DatabaseEntitySingle(
                                rootState = task.rootState,
                                subState = TaskStatus.CANCELED.name,
                                task = SingleTaskEntity(task.allTasks.canceled)
                            )
                        )
                    }

                    ongoingTaskDeferred.await()
                    doneTaskDeferred.await()
                    canceledTaskDeferred.await()
                }
            }
        }
    }


    @OptIn(DelicateCoroutinesApi::class)
    suspend fun insertTaskDataUpdated(
        task: TasksV2DatabaseEntity,
        localSubState: String
    ) {
        when (task.rootState) {
            TaskRootStateTags.ToMe.tagValue -> {
                when (localSubState) {
                    "new" -> {
                        println("TaskListInsert -> To-Me = New ${task.allTasks.new.size}")
                        taskDao.insertTaskDataWithState(
                            TasksV2DatabaseEntitySingle(
                                rootState = task.rootState,
                                subState = TaskStatus.NEW.name,
                                task = SingleTaskEntity(task.allTasks.new)
                            )
                        )
                    }

                    "ongoing" -> {
                        println("TaskListInsert -> To-Me = ongoing ${task.allTasks.ongoing.size}")
                        taskDao.insertTaskDataWithState(
                            TasksV2DatabaseEntitySingle(
                                rootState = task.rootState,
                                subState = TaskStatus.ONGOING.name,
                                task = SingleTaskEntity(task.allTasks.ongoing)
                            )
                        )
                    }

                    "done" -> {
                        println("TaskListInsert -> To-Me = DONE ${task.allTasks.done.size}")
                        taskDao.insertTaskDataWithState(
                            TasksV2DatabaseEntitySingle(
                                rootState = task.rootState,
                                subState = TaskStatus.DONE.name,
                                task = SingleTaskEntity(task.allTasks.done)
                            )
                        )
                    }
                }
            }

            TaskRootStateTags.FromMe.tagValue -> {
                when (localSubState) {
                    "unread" -> {
                        taskDao.insertTaskDataWithState(
                            TasksV2DatabaseEntitySingle(
                                rootState = task.rootState,
                                subState = TaskStatus.UNREAD.name,
                                task = SingleTaskEntity(task.allTasks.unread)
                            )
                        )
                    }

                    "ongoing" -> {
                        taskDao.insertTaskDataWithState(
                            TasksV2DatabaseEntitySingle(
                                rootState = task.rootState,
                                subState = TaskStatus.ONGOING.name,
                                task = SingleTaskEntity(task.allTasks.ongoing)
                            )
                        )
                    }

                    "done" -> {
                        taskDao.insertTaskDataWithState(
                            TasksV2DatabaseEntitySingle(
                                rootState = task.rootState,
                                subState = TaskStatus.DONE.name,
                                task = SingleTaskEntity(task.allTasks.done)
                            )
                        )
                    }
                }
            }

            TaskRootStateTags.Hidden.tagValue -> {
                when (localSubState) {
                    "ongoing" -> {
                        taskDao.insertTaskDataWithState(
                            TasksV2DatabaseEntitySingle(
                                rootState = task.rootState,
                                subState = TaskStatus.ONGOING.name,
                                task = SingleTaskEntity(task.allTasks.ongoing)
                            )
                        )
                    }

                    "done" -> {
                        taskDao.insertTaskDataWithState(
                            TasksV2DatabaseEntitySingle(
                                rootState = task.rootState,
                                subState = TaskStatus.DONE.name,
                                task = SingleTaskEntity(task.allTasks.done)
                            )
                        )
                    }

                    "canceled" -> {
                        taskDao.insertTaskDataWithState(
                            TasksV2DatabaseEntitySingle(
                                rootState = task.rootState,
                                subState = TaskStatus.CANCELED.name,
                                task = SingleTaskEntity(task.allTasks.canceled)
                            )
                        )
                    }
                }
            }
        }
    }

}