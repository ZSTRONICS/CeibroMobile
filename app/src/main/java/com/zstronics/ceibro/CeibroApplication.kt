package com.zstronics.ceibro

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.work.Configuration
import androidx.work.WorkManager
import com.gu.toolargetool.TooLargeTool
import com.onesignal.OneSignal
import com.zstronics.ceibro.data.base.interceptor.SessionValidator
import com.zstronics.ceibro.data.database.models.inbox.CeibroInboxV2
import com.zstronics.ceibro.data.database.models.projects.CeibroProjectV2
import com.zstronics.ceibro.data.database.models.tasks.CeibroTaskV2
import com.zstronics.ceibro.data.database.models.tasks.Events
import com.zstronics.ceibro.data.database.models.tasks.LocalTaskDetailFiles
import com.zstronics.ceibro.data.database.models.tasks.TaskFiles
import com.zstronics.ceibro.data.repos.NotificationTaskData
import com.zstronics.ceibro.data.repos.auth.IAuthRepository
import com.zstronics.ceibro.data.repos.auth.login.Tokens
import com.zstronics.ceibro.data.repos.projects.drawing.DrawingV2
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.ui.socket.SocketHandler
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.Executors
import javax.inject.Inject

@HiltAndroidApp
open class CeibroApplication : Application() {
    @Inject
    lateinit var sessionValidator: SessionValidator

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var authApi: IAuthRepository

    override fun onCreate() {
        super.onCreate()
        ceibroApplication = this
        sessionValidator.setAuthRepository(authApi)
        sessionValidator.setSessionManager(sessionManager)

        // Logging set to help debug issues, remove before releasing your app.
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE)
        TooLargeTool.startLogging(this)
        // OneSignal Initialization
        OneSignal.initWithContext(this)
        OneSignal.setAppId(BuildConfig.ONE_SIGNAL_ID)
        val customExecutor = Executors.newSingleThreadExecutor { runnable ->
            val thread = Thread(runnable)
            thread.priority = android.os.Process.THREAD_PRIORITY_BACKGROUND
            thread
        }

        val workManagerConfiguration = Configuration.Builder()
            .setExecutor(customExecutor)
            .build()

        WorkManager.initialize(applicationContext, workManagerConfiguration)
    }

    companion object {
        var ceibroApplication: CeibroApplication? = null
            private set
        var isNetworkObserverRegistered = false
    }

    override fun onTerminate() {
        super.onTerminate()
//        SocketHandler.sendLogout()
        SocketHandler.closeConnectionAndRemoveObservers()
    }

    object CookiesManager {
        var navigationGraphStartDestination: Int = 0
        var jwtToken: String? = null
        var isLoggedIn: Boolean = false
        var tokens: Tokens? = null
        var secureUUID: String? = null
        var deviceType: String? = null
        var androidId: String? = null
        var allInboxTasks: MutableLiveData<MutableList<CeibroInboxV2>> = MutableLiveData()
        var inboxTasksSortingType: MutableLiveData<String> = MutableLiveData("")
        var rootOngoingAllTasks: MutableLiveData<MutableList<CeibroTaskV2>> = MutableLiveData()
        var rootOngoingToMeTasks: MutableLiveData<MutableList<CeibroTaskV2>> = MutableLiveData()
        var rootOngoingFromMeTasks: MutableLiveData<MutableList<CeibroTaskV2>> = MutableLiveData()
        var rootApprovalAllTasks: MutableLiveData<MutableList<CeibroTaskV2>> = MutableLiveData()
        var rootApprovalInReviewPendingTasks: MutableLiveData<MutableList<CeibroTaskV2>> =
            MutableLiveData()
        var rootApprovalToReviewTasks: MutableLiveData<MutableList<CeibroTaskV2>> =
            MutableLiveData()
        var rootClosedAllTasks: MutableLiveData<MutableList<CeibroTaskV2>> = MutableLiveData()
        var rootClosedToMeTasks: MutableLiveData<MutableList<CeibroTaskV2>> = MutableLiveData()
        var rootClosedFromMeTasks: MutableLiveData<MutableList<CeibroTaskV2>> = MutableLiveData()
        var toMeNewTasks: MutableLiveData<MutableList<CeibroTaskV2>> = MutableLiveData()
        var toMeOngoingTasks: MutableLiveData<MutableList<CeibroTaskV2>> = MutableLiveData()
        var toMeDoneTasks: MutableLiveData<MutableList<CeibroTaskV2>> = MutableLiveData()
        var fromMeUnreadTasks: MutableLiveData<MutableList<CeibroTaskV2>> = MutableLiveData()
        var fromMeOngoingTasks: MutableLiveData<MutableList<CeibroTaskV2>> = MutableLiveData()
        var fromMeDoneTasks: MutableLiveData<MutableList<CeibroTaskV2>> = MutableLiveData()
        var hiddenCanceledTasks: MutableLiveData<MutableList<CeibroTaskV2>> = MutableLiveData()
        var hiddenOngoingTasks: MutableLiveData<MutableList<CeibroTaskV2>> = MutableLiveData()
        var hiddenDoneTasks: MutableLiveData<MutableList<CeibroTaskV2>> = MutableLiveData()
        var taskDataForDetails: CeibroTaskV2? = null
        var taskDataForDetailsFromNotification: CeibroTaskV2? = null
        var taskDetailEvents: List<Events>? = null
        var taskDetailFiles: MutableLiveData<List<LocalTaskDetailFiles>?> = MutableLiveData(null)
        var taskDetailRootState: String? = null
        var taskDetailSelectedSubState: String? = null
        var taskIdInDetails: String = ""
        val appFirstStartForSocket: MutableLiveData<Boolean> = MutableLiveData(true)
        val socketOnceConnected: MutableLiveData<Boolean> = MutableLiveData(false)
        var projectDataForDetails: CeibroProjectV2? = null
        var projectNameForDetails: String = ""
        var locationProjectDataForDetails: CeibroProjectV2? = null
        var locationProjectNameForDetails: String = ""
        var notificationDataContent: MutableList<Pair<NotificationTaskData, Int>> = mutableListOf()
        var drawingFileForLocation: MutableLiveData<DrawingV2> = MutableLiveData()
        var drawingFileForNewTask: MutableLiveData<DrawingV2> = MutableLiveData()
        var cameToLocationViewFromProject: Boolean = false
        var openingNewLocationFile: Boolean = false
        var openKeyboardWithLocalFile: OpenKeyboardWithLocalFile? = null
        var openKeyboardWithFile: OpenKeyboardWithFile? = null
    }

    class OpenKeyboardWithLocalFile(val item: LocalTaskDetailFiles, val type: String)
    open class OpenKeyboardWithFile(val item: TaskFiles, val type: String)
}