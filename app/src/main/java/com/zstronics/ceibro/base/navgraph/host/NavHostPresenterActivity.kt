package com.zstronics.ceibro.base.navgraph.host

import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.viewModels
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.databinding.ActivityNavhostPresenterBinding
import com.zstronics.ceibro.ui.networkobserver.NetworkConnectivityObserver
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.aviran.cookiebar2.CookieBar
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
    }

    companion object {
        var activityInstance: NavHostPresenterActivity? = null
        var isForceCancelled: Boolean = false
    }

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        activityInstance = this
    }

    override fun postExecutePendingBindings(savedInstanceState: Bundle?) {
        super.postExecutePendingBindings(savedInstanceState)
        viewModel.viewModelScope.launch {
            networkConnectivityObserver.observe().map {
                it == NetworkConnectivityObserver.Status.Available
            }.collect {
                if (isForceCancelled) return@collect
                CookieBar.build(this@NavHostPresenterActivity)
                    .setTitle("")
                    .setCookiePosition(CookieBar.TOP)
                    .setMessage(if (it) R.string.you_re_back_online else R.string.internet_is_not_connected)
                    .setBackgroundColor(if (it) R.color.appGreen else R.color.appRed)
                    .setAction(if (it) "" else getString(R.string.close)) {
                        isForceCancelled = true
                    }
                    .setDuration(5000) // 5 seconds
                    .show()
            }

        }
    }

    override fun onClick(id: Int) {
    }

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
}