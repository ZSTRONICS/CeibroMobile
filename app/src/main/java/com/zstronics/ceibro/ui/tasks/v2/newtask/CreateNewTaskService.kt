package com.zstronics.ceibro.ui.tasks.v2.newtask

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import androidx.room.RoomDatabase
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.host.NavHostPresenterActivity
import com.zstronics.ceibro.data.base.CookiesManager
import com.zstronics.ceibro.data.database.CeibroDatabase
import com.zstronics.ceibro.data.database.dao.TaskV2Dao
import com.zstronics.ceibro.data.database.models.tasks.CeibroTaskV2
import com.zstronics.ceibro.data.repos.task.TaskRepository
import com.zstronics.ceibro.data.repos.task.models.v2.NewTaskV2Entity
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.data.sessions.SharedPreferenceManager
import com.zstronics.ceibro.ui.dashboard.SharedViewModel
import com.zstronics.ceibro.ui.socket.LocalEvents
import com.zstronics.ceibro.ui.tasks.task.TaskStatus
import com.zstronics.ceibro.ui.tasks.v2.newtask.CreateNewTaskService.NotificationUtils.CHANNEL_ID
import com.zstronics.ceibro.ui.tasks.v2.newtask.CreateNewTaskService.NotificationUtils.CHANNEL_NAME
import com.zstronics.ceibro.ui.tasks.v2.newtask.NewTaskV2VM.Companion.taskList
import com.zstronics.ceibro.ui.tasks.v2.newtask.NewTaskV2VM.Companion.taskRequest
import dagger.hilt.android.AndroidEntryPoint
import ee.zstronics.ceibro.camera.PickedImages
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject

@AndroidEntryPoint
class CreateNewTaskService : Service() {

    private var taskObjectData: NewTaskV2Entity? = null
    private var taskListData: ArrayList<PickedImages>? = null
    var sessionManager: SessionManager? = null
    private lateinit var mContext: Context
    private val indeterminateNotificationID = 1

    @Inject
    lateinit var taskRepository: TaskRepository

    override fun onCreate() {
        super.onCreate()
        taskObjectData = taskRequest
        taskListData = taskList
        mContext = this

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val action = intent?.getStringExtra("taskRequest")
        if (action == "ServiceCall") {
            Handler(Looper.getMainLooper()).postDelayed({
                sessionManager = getSessionManager(SharedPreferenceManager(this))

                startForeground(
                    indeterminateNotificationID, createIndeterminateNotificationForFileUpload(
                        activity = this,
                        channelId = CHANNEL_ID,
                        channelName = CHANNEL_NAME,
                        notificationTitle = "Creating task with files"
                    )
                )
                createTask(sessionManager, mContext)
            }, 100)
        }

        return START_STICKY
    }

    private fun createTask(sessionManager: SessionManager?, context: Context) {

        taskObjectData?.let { taskRequestData ->
            taskListData?.let { taskListData ->
                newTaskV2WithFiles(
                    taskRequestData, taskListData, context, sessionManager
                )
            }
        }

    }

    private fun newTaskV2WithFiles(
        newTask: NewTaskV2Entity,
        list: ArrayList<PickedImages>?,
        context: Context,
        sessionManager: SessionManager?
    ) {
        GlobalScope.launch {
            sessionManager?.let {
                createTaskWithFiles(newTask, list, sessionManager, context)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


    private fun updateCreatedTaskInLocal(
        task: CeibroTaskV2?, taskDao: TaskV2Dao, sessionManager: SessionManager
    ) {
        val sharedViewModel = NavHostPresenterActivity.activityInstance?.let {
            ViewModelProvider(it).get(SharedViewModel::class.java)
        }

        task?.let { newTask ->
            GlobalScope.launch {
                sessionManager.saveUpdatedAtTimeStamp(newTask.updatedAt)
                taskDao.insertTaskData(newTask)

                if (newTask.isCreator) {
                    when (newTask.fromMeState) {
                        TaskStatus.UNREAD.name.lowercase() -> {
                            val allFromMeUnreadTasks =
                                CookiesManager.fromMeUnreadTasks.value ?: mutableListOf()
                            val foundTask = allFromMeUnreadTasks.find { it.id == newTask.id }
                            if (foundTask != null) {
                                val index = allFromMeUnreadTasks.indexOf(foundTask)
                                allFromMeUnreadTasks.removeAt(index)
                            }
                            allFromMeUnreadTasks.add(newTask)
                            val unreadTasks =
                                allFromMeUnreadTasks.sortedByDescending { it.updatedAt }
                                    .toMutableList()
                            CookiesManager.fromMeUnreadTasks.postValue(unreadTasks)
                        }

                        TaskStatus.ONGOING.name.lowercase() -> {
                            val allFromMeOngoingTasks =
                                CookiesManager.fromMeOngoingTasks.value ?: mutableListOf()
                            val foundTask = allFromMeOngoingTasks.find { it.id == newTask.id }
                            if (foundTask != null) {
                                val index = allFromMeOngoingTasks.indexOf(foundTask)
                                allFromMeOngoingTasks.removeAt(index)
                            }
                            allFromMeOngoingTasks.add(newTask)
                            val ongoingTasks =
                                allFromMeOngoingTasks.sortedByDescending { it.updatedAt }
                                    .toMutableList()
                            CookiesManager.fromMeOngoingTasks.postValue(ongoingTasks)
                        }

                        TaskStatus.DONE.name.lowercase() -> {
                            val allFromMeDoneTasks =
                                CookiesManager.fromMeDoneTasks.value ?: mutableListOf()
                            val foundTask = allFromMeDoneTasks.find { it.id == newTask.id }
                            if (foundTask != null) {
                                val index = allFromMeDoneTasks.indexOf(foundTask)
                                allFromMeDoneTasks.removeAt(index)
                            }
                            allFromMeDoneTasks.add(newTask)
                            val doneTasks = allFromMeDoneTasks.sortedByDescending { it.updatedAt }
                                .toMutableList()
                            CookiesManager.fromMeDoneTasks.postValue(doneTasks)
                        }
                    }
                }

                if (newTask.isAssignedToMe) {
                    when (newTask.toMeState) {
                        TaskStatus.NEW.name.lowercase() -> {
                            val allToMeNewTasks =
                                CookiesManager.toMeNewTasks.value ?: mutableListOf()
                            val foundTask = allToMeNewTasks.find { it.id == newTask.id }
                            if (foundTask != null) {
                                val index = allToMeNewTasks.indexOf(foundTask)
                                allToMeNewTasks.removeAt(index)
                            }
                            allToMeNewTasks.add(newTask)
                            val newTasks =
                                allToMeNewTasks.sortedByDescending { it.updatedAt }.toMutableList()
                            CookiesManager.toMeNewTasks.postValue(newTasks)
                        }

                        TaskStatus.ONGOING.name.lowercase() -> {
                            val allToMeOngoingTasks =
                                CookiesManager.toMeOngoingTasks.value ?: mutableListOf()
                            val foundTask = allToMeOngoingTasks.find { it.id == newTask.id }
                            if (foundTask != null) {
                                val index = allToMeOngoingTasks.indexOf(foundTask)
                                allToMeOngoingTasks.removeAt(index)
                            }
                            allToMeOngoingTasks.add(newTask)
                            val ongoingTasks =
                                allToMeOngoingTasks.sortedByDescending { it.updatedAt }
                                    .toMutableList()
                            CookiesManager.toMeOngoingTasks.postValue(ongoingTasks)
                        }

                        TaskStatus.DONE.name.lowercase() -> {
                            val allToMeDoneTasks =
                                CookiesManager.toMeDoneTasks.value ?: mutableListOf()
                            val foundTask = allToMeDoneTasks.find { it.id == newTask.id }
                            if (foundTask != null) {
                                val index = allToMeDoneTasks.indexOf(foundTask)
                                allToMeDoneTasks.removeAt(index)
                            }
                            allToMeDoneTasks.add(newTask)
                            val doneTasks =
                                allToMeDoneTasks.sortedByDescending { it.updatedAt }.toMutableList()
                            CookiesManager.toMeDoneTasks.postValue(doneTasks)
                        }
                    }
                    sharedViewModel?.isToMeUnread?.value = true
                    sessionManager.saveToMeUnread(true)
                }

                EventBus.getDefault().post(LocalEvents.RefreshTasksData())

            }
        }

    }

    private fun providesAppDatabase(context: Context): CeibroDatabase {
        return Room.databaseBuilder(context, CeibroDatabase::class.java, CeibroDatabase.DB_NAME)
            .addCallback(object : RoomDatabase.Callback() {
            })
            .fallbackToDestructiveMigration().build()
    }

    private fun getSessionManager(
        sharedPreferenceManager: SharedPreferenceManager
    ) = SessionManager(sharedPreferenceManager)

    private fun hideIndeterminateNotificationForFileUpload(activity: Context) {
        val notificationManager = activity.getSystemService(NotificationManager::class.java)
        notificationManager.cancel(indeterminateNotificationID) // Remove the notification with ID
    }


    object NotificationUtils {
        const val CHANNEL_ID = "file_upload_channel"
        const val CHANNEL_NAME = "Create Progress Channel"
    }


    private fun createIndeterminateNotificationForFileUpload(
        activity: CreateNewTaskService,
        channelId: String,
        channelName: String,
        notificationTitle: String,
        isOngoing: Boolean = true,
        indeterminate: Boolean = true,
        notificationIcon: Int = R.drawable.icon_upload
    ): Notification {
        val channel =
            NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
        val notificationManager = activity.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)

        val builder = NotificationCompat.Builder(activity, channelId).setSmallIcon(notificationIcon)
            .setContentTitle(notificationTitle).setOngoing(isOngoing)
            .setProgress(0, 0, indeterminate)

        notificationManager.notify(indeterminateNotificationID, builder.build())
        return builder.build()
    }


    private suspend fun createTaskWithFiles(
        newTask: NewTaskV2Entity,
        list: ArrayList<PickedImages>?,
        sessionManager: SessionManager,
        context: Context
    ) {

        list?.let {
            taskRepository.newTaskV2WithFiles(
                newTask,
                it
            ) { isSuccess, task, errorMessage ->
                if (isSuccess) {

                    val room = providesAppDatabase(context)
                    val taskDao = room.getTaskV2sDao()
                    this.sessionManager?.let {
                        updateCreatedTaskInLocal(task, taskDao, sessionManager)
                    }
                    hideIndeterminateNotificationForFileUpload(context)
                    stopSelf()
                } else {
                    hideIndeterminateNotificationForFileUpload(context)
                    stopSelf()
                }
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        println("Service is destroyed...")
    }
}