package com.zstronics.ceibro.base.navgraph.host

import android.os.Bundle
import android.os.Handler
import android.os.PersistableBundle
import android.view.View
import androidx.activity.viewModels
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.work.WorkManager
import com.bumptech.glide.Glide
import com.onesignal.OneSignal
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.CeibroApplication
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.KEY_APP_FIRST_RUN_FOR_INTERNET
import com.zstronics.ceibro.base.KEY_LAST_OFFLINE
import com.zstronics.ceibro.base.KEY_SOCKET_OBSERVER_SET
import com.zstronics.ceibro.base.extensions.launchActivityWithFinishAffinityForActivity
import com.zstronics.ceibro.data.database.dao.ConnectionGroupV2Dao
import com.zstronics.ceibro.data.database.dao.ConnectionsV2Dao
import com.zstronics.ceibro.data.database.dao.DraftNewTaskV2Dao
import com.zstronics.ceibro.data.database.dao.DrawingPinsV2Dao
import com.zstronics.ceibro.data.database.dao.FloorsV2Dao
import com.zstronics.ceibro.data.database.dao.GroupsV2Dao
import com.zstronics.ceibro.data.database.dao.InboxV2Dao
import com.zstronics.ceibro.data.database.dao.ProjectsV2Dao
import com.zstronics.ceibro.data.database.dao.TaskDetailFilesV2Dao
import com.zstronics.ceibro.data.database.dao.TaskV2Dao
import com.zstronics.ceibro.data.database.dao.TopicsV2Dao
import com.zstronics.ceibro.data.repos.task.TaskRepository
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.data.sessions.SharedPreferenceManager
import com.zstronics.ceibro.databinding.ActivityNavhostPresenterBinding
import com.zstronics.ceibro.ui.contacts.ContactSyncWorker
import com.zstronics.ceibro.ui.locationv2.LocationsV2Fragment
import com.zstronics.ceibro.ui.networkobserver.NetworkConnectivityObserver
import com.zstronics.ceibro.ui.socket.LocalEvents
import com.zstronics.ceibro.ui.socket.SocketHandler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.IOException
import java.net.InetAddress
import javax.inject.Inject

const val NAVIGATION_Graph_ID = "navigationGraphId"
const val NAVIGATION_Graph_START_DESTINATION_ID = "navigationGraphStartDestination"

@AndroidEntryPoint
class NavHostPresenterActivity :
    BaseNavViewModelActivity<ActivityNavhostPresenterBinding, INavHostPresenter.State, NavHostPresenterVM>() {
    override val navigationGraphId: Int
        get() = intent?.getIntExtra(NAVIGATION_Graph_ID, 0) ?: 0
    override val navigationGraphStartDestination: Int
        get() = intent?.getIntExtra(NAVIGATION_Graph_START_DESTINATION_ID, 0) ?: 0
    override val viewModel: NavHostPresenterVM by viewModels()
    override val layoutResId: Int = R.layout.activity_navhost_presenter

    @Inject
    lateinit var taskRepository: TaskRepository
    @Inject
    lateinit var taskDao: TaskV2Dao
    @Inject
    lateinit var topicsV2Dao: TopicsV2Dao
    @Inject
    lateinit var projectsV2Dao: ProjectsV2Dao
    @Inject
    lateinit var floorV2Dao: FloorsV2Dao
    @Inject
    lateinit var inboxV2Dao: InboxV2Dao
    @Inject
    lateinit var groupV2Dao: GroupsV2Dao
    @Inject
    lateinit var connectionsV2Dao: ConnectionsV2Dao
    @Inject
    lateinit var connectionGroupV2Dao: ConnectionGroupV2Dao
    @Inject
    lateinit var taskDetailFilesV2Dao: TaskDetailFilesV2Dao
    @Inject
    lateinit var drawingPinsDao: DrawingPinsV2Dao
    @Inject
    lateinit var draftNewTaskV2Dao: DraftNewTaskV2Dao

    @Inject
    lateinit var networkConnectivityObserver: NetworkConnectivityObserver
    override fun onDestinationChanged(
        controller: NavController?,
        destination: NavDestination?,
        arguments: Bundle?
    ) {
        CeibroApplication.CookiesManager.navigationGraphStartDestination = destination?.id ?: 0
    }

    companion object {
        var activityInstance: NavHostPresenterActivity? = null
        var isDrawingLoaded = false
        const val BANNER_HIDE_TIME: Long = 3000
    }

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        activityInstance = this
    }

    override fun postExecutePendingBindings(savedInstanceState: Bundle?) {
        super.postExecutePendingBindings(savedInstanceState)
        if (navigationGraphStartDestination == R.id.ceibroDataLoadingFragment || navigationGraphStartDestination == R.id.loginFragment) {
            //Do nothing
        } else {
            viewModel.viewModelScope.launch {
                networkConnectivityObserver.observe().collect { connectionStatus ->
                    val sessionManager =
                        getSessionManager(SharedPreferenceManager(applicationContext))
                    val isAppFirstRun =
                        sessionManager.getBooleanValue(KEY_APP_FIRST_RUN_FOR_INTERNET)
                    val isLastOffline =
                        sessionManager.getBooleanValue(KEY_LAST_OFFLINE)
                    println("Heartbeat, InternetStatus: $connectionStatus -> Last Offline $isLastOffline")
                    when (connectionStatus) {
                        NetworkConnectivityObserver.Status.Losing -> {
                            // Do not remove this losing state from here
                            if (isAppFirstRun) {
                                sessionManager.saveBooleanValue(
                                    KEY_APP_FIRST_RUN_FOR_INTERNET,
                                    false
                                )
                            }
                            sessionManager.saveBooleanValue(
                                KEY_LAST_OFFLINE,
                                true
                            )
                            isPingableToServer { pingable ->
                                if (pingable) {
                                    if (isAppFirstRun.not()) {
                                        if (navigationGraphStartDestination != R.id.editProfileFragment || mViewDataBinding.llInternetDisconnected.visibility == View.VISIBLE) {
                                            if (isLastOffline) {
                                                sessionManager.saveBooleanValue(
                                                    KEY_LAST_OFFLINE,
                                                    false
                                                )
                                                mViewDataBinding.llInternetConnected.visibility =
                                                    View.VISIBLE
                                                mViewDataBinding.llInternetDisconnected.visibility =
                                                    View.GONE

                                                launch {
                                                    delay(BANNER_HIDE_TIME)
                                                }
                                                // After the delay, hide the views
                                                mViewDataBinding.llInternetConnected.visibility =
                                                    View.GONE
                                            }
                                        }
                                    }
                                } else {
                                    mViewDataBinding.llInternetConnected.visibility = View.GONE
                                    mViewDataBinding.llInternetDisconnected.visibility =
                                        View.VISIBLE
                                }
                            }
                        }

                        NetworkConnectivityObserver.Status.Available -> {
                            if (isAppFirstRun.not()) {
                                if (navigationGraphStartDestination != R.id.editProfileFragment || mViewDataBinding.llInternetDisconnected.visibility == View.VISIBLE) {
                                    if (isLastOffline) {
                                        sessionManager.saveBooleanValue(KEY_LAST_OFFLINE, false)
                                        mViewDataBinding.llInternetConnected.visibility =
                                            View.VISIBLE
                                        mViewDataBinding.llInternetDisconnected.visibility =
                                            View.GONE

                                        delay(BANNER_HIDE_TIME)
                                        // After the delay, hide the views
                                        mViewDataBinding.llInternetConnected.visibility = View.GONE
                                    } else {
                                        mViewDataBinding.llInternetDisconnected.visibility =
                                            View.GONE
                                        mViewDataBinding.llInternetConnected.visibility = View.GONE
                                    }
                                }
                            }
                            sessionManager.saveBooleanValue(KEY_APP_FIRST_RUN_FOR_INTERNET, false)
                            sessionManager.saveBooleanValue(KEY_LAST_OFFLINE, false)
                        }

                        else -> {
                            if (isAppFirstRun) {
                                sessionManager.saveBooleanValue(
                                    KEY_APP_FIRST_RUN_FOR_INTERNET,
                                    false
                                )
                            }
                            sessionManager.saveBooleanValue(
                                KEY_LAST_OFFLINE,
                                true
                            )
                            isPingableToServer { pingable ->
                                if (pingable) {
                                    if (isAppFirstRun.not()) {
                                        if (navigationGraphStartDestination != R.id.editProfileFragment || mViewDataBinding.llInternetDisconnected.visibility == View.VISIBLE) {
                                            if (isLastOffline) {
                                                sessionManager.saveBooleanValue(
                                                    KEY_LAST_OFFLINE,
                                                    false
                                                )
                                                mViewDataBinding.llInternetConnected.visibility =
                                                    View.VISIBLE
                                                mViewDataBinding.llInternetDisconnected.visibility =
                                                    View.GONE

                                                launch {
                                                    delay(BANNER_HIDE_TIME)
                                                }
                                                // After the delay, hide the views
                                                mViewDataBinding.llInternetConnected.visibility =
                                                    View.GONE
                                            }
                                        }
                                    }
                                } else {
                                    mViewDataBinding.llInternetConnected.visibility = View.GONE
                                    mViewDataBinding.llInternetDisconnected.visibility =
                                        View.VISIBLE
                                }
                            }
                        }
                    }
                }
            }
        }
        mViewDataBinding.closeBanner.setOnClickListener {
            mViewDataBinding.llInternetDisconnected.visibility = View.GONE
        }
    }

    private fun isPingableToServer(callback: (Boolean) -> Unit) {
        GlobalScope.launch {
            try {
                // You can use either a Google URL or DNS address
                val address = withContext(Dispatchers.IO) {
                    InetAddress.getByName("www.google.com")
                }
                // Or use the Google DNS IP addresses: "8.8.8.8" or "8.8.4.4"
                // val address = InetAddress.getByName("8.8.8.8")

                // Timeout in milliseconds for the ping
                val timeoutMs = 1500

                // Attempt to reach the address within the specified timeout
                val isReachable =
                    withContext(Dispatchers.IO) {
                        address.isReachable(timeoutMs)
                    }
                withContext(Dispatchers.Main) {
                    callback(isReachable)
                }
            } catch (e: IOException) {
                // An exception is thrown if the address is not reachable
                withContext(Dispatchers.Main) {
                    callback(false)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (applicationContext != null) {
            val sessionManager =
                getSessionManager(SharedPreferenceManager(applicationContext))
            println("Heartbeat -> NavHostPresenterActivity jwtToken.isNullOrEmpty ${CeibroApplication.CookiesManager.jwtToken.isNullOrEmpty()}")
            if (sessionManager.isLoggedIn()) {
                if (CeibroApplication.CookiesManager.jwtToken.isNullOrEmpty()) {
                    sessionManager.setToken()
                    println("Heartbeat -> NavHostPresenterActivity jwtToken set")
                }
                if (sessionManager.getUser().value?.id.isNullOrEmpty()) {
                    sessionManager.setUser()
                    println("Heartbeat -> NavHostPresenterActivity User set")
                }
            }

            if (navigationGraphStartDestination == R.id.ceibroDataLoadingFragment || navigationGraphStartDestination == R.id.loginFragment ||
                navigationGraphStartDestination == R.id.registerFragment || navigationGraphStartDestination == R.id.verifyNumberFragment ||
                navigationGraphStartDestination == R.id.termsFragment || navigationGraphStartDestination == R.id.signUpFragment ||
                navigationGraphStartDestination == R.id.photoFragment || navigationGraphStartDestination == R.id.contactsSelectionFragment ||
                navigationGraphStartDestination == R.id.forgotPasswordFragment
            ) {
                //Do nothing
            } else {
                Handler().postDelayed({
                    println(
                        "Heartbeat, NavHostPresenterActivity... connected = ${
                            SocketHandler.getSocket()?.connected()
                        }"
                    )
                    if (SocketHandler.getSocket() == null || SocketHandler.getSocket()
                            ?.connected() == null || SocketHandler.getSocket()?.connected() == false
                    ) {
                        if (SocketHandler.getSocket() == null || SocketHandler.getSocket()
                                ?.connected() == null
                        ) {
                            println("Heartbeat, Internet observer Socket setting new in NavHostPresenterActivity")
                            SocketHandler.setActivityContext(this)
                            SocketHandler.closeConnectionAndRemoveObservers()
                            SocketHandler.setSocket()
                            if (navigationGraphStartDestination != R.id.homeFragment) {
                                sessionManager.saveBooleanValue(KEY_SOCKET_OBSERVER_SET, false)
                            }
                        }
                        SocketHandler.establishConnection()
                    }
                }, 2600)
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onLogoutUnAuthorizedUser(event: LocalEvents.LogoutUnAuthorizedUser) {
        logoutUser()
    }

    override fun onStart() {
        super.onStart()
        try {
            EventBus.getDefault().register(this)
        } catch (_: Exception) {
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            EventBus.getDefault().unregister(this)
        } catch (_: Exception) {
        }
    }

    private fun logoutUser() {
        val sessionManager =
            getSessionManager(SharedPreferenceManager(applicationContext))
        val oneSignalPlayerId = OneSignal.getDeviceState()?.userId
        SocketHandler.sendLogout(oneSignalPlayerId)
        GlobalScope.launch {
            taskRepository.eraseTaskTable()
            taskRepository.eraseSubTaskTable()
            taskDao.deleteAllEventsData()
            taskDao.deleteAllTasksData()
            topicsV2Dao.deleteAllData()
            projectsV2Dao.deleteAll()
            groupV2Dao.deleteAll()
            floorV2Dao.deleteAll()
            inboxV2Dao.deleteAll()
            connectionsV2Dao.deleteAll()
            connectionGroupV2Dao.deleteAll()
            taskDetailFilesV2Dao.deleteAll()
            draftNewTaskV2Dao.deleteAllData()
            drawingPinsDao.deleteAll()
        }
        sessionManager.endUserSession()
        // Cancel all periodic work with the tag "contactSync"
        WorkManager.getInstance(this)
            .cancelAllWorkByTag(ContactSyncWorker.CONTACT_SYNC_WORKER_TAG)

        launchActivityWithFinishAffinityForActivity<NavHostPresenterActivity>(
            options = Bundle(),
            clearPrevious = true
        ) {
            putExtra(NAVIGATION_Graph_ID, R.navigation.onboarding_nav_graph)
            putExtra(
                NAVIGATION_Graph_START_DESTINATION_ID,
                R.id.loginFragment
            )
        }
        Thread { this.let { Glide.get(it).clearDiskCache() } }.start()
    }

    private fun getSessionManager(
        sharedPreferenceManager: SharedPreferenceManager
    ) = SessionManager(sharedPreferenceManager)

    override fun onClick(id: Int) {
    }

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
}