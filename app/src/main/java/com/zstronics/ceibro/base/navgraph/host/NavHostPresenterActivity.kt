package com.zstronics.ceibro.base.navgraph.host

import android.os.Bundle
import android.os.Handler
import android.os.PersistableBundle
import android.view.View
import androidx.activity.viewModels
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.KEY_APP_FIRST_RUN_FOR_INTERNET
import com.zstronics.ceibro.base.KEY_LAST_OFFLINE
import com.zstronics.ceibro.base.KEY_SOCKET_OBSERVER_SET
import com.zstronics.ceibro.data.base.CookiesManager
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.data.sessions.SharedPreferenceManager
import com.zstronics.ceibro.databinding.ActivityNavhostPresenterBinding
import com.zstronics.ceibro.ui.networkobserver.NetworkConnectivityObserver
import com.zstronics.ceibro.ui.socket.SocketHandler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    lateinit var networkConnectivityObserver: NetworkConnectivityObserver
    override fun onDestinationChanged(
        controller: NavController?,
        destination: NavDestination?,
        arguments: Bundle?
    ) {
        CookiesManager.navigationGraphStartDestination = destination?.id ?: 0
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
            println("Heartbeat -> NavHostPresenterActivity jwtToken.isNullOrEmpty ${CookiesManager.jwtToken.isNullOrEmpty()}")
            if (sessionManager.isLoggedIn()) {
                if (CookiesManager.jwtToken.isNullOrEmpty()) {
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

    private fun getSessionManager(
        sharedPreferenceManager: SharedPreferenceManager
    ) = SessionManager(sharedPreferenceManager)

    override fun onClick(id: Int) {
    }

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
}